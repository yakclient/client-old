package net.yakclient.client.boot.internal

import net.yakclient.client.boot.repository.Dependency
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType
import net.yakclient.client.util.get
import net.yakclient.client.util.isReachable
import net.yakclient.client.util.openStream
import org.w3c.dom.Node
import java.net.URI
import java.util.logging.Logger
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

private const val DEFAULT_SCOPE = "runtime"

public class MavenRepositoryHandler(override val settings: RepositorySettings) : RepositoryHandler {
    private val logger = Logger.getLogger(settings.name ?: "anonymous-maven-logger")
    private val repo = when (settings.type) {
        RepositoryType.MAVEN -> settings.path
        RepositoryType.MAVEN_CENTRAL -> "https://repo.maven.apache.org/maven2"
        else -> throw IllegalArgumentException("Unknown repo type: ${settings.type}")
    }

    override fun find(it: String): Dependency? {
        open class MavenProject(
            val group: String,
            val artifact: String,
            val version: String,
        )

        class MavenDependency(
            group: String, artifact: String, version: String,
            val scope: String,
        ) : MavenProject(group, artifact, version)

        data class MavenArtifact(
            val artifact: URI,
            val jar: URI,
            val pom: URI
        )

        val project = it.split(':').let { MavenProject(it[0], it[1], it[2]) }

        fun loadArtifact(proj: MavenProject): MavenArtifact =
            "$repo/${proj.group.replace('.', '/')}/${proj.artifact}".let {
                MavenArtifact(
                    URI(it),
                    URI("$it/${proj.version}/${proj.artifact}-${proj.version}.jar"),
                    URI("$it/${proj.version}/${proj.artifact}-${proj.version}.pom")
                )
            }

        val (artifact, jar, pom) = loadArtifact(project)
        println(pom)

        if (!artifact.toURL().isReachable()) return null

        val factory = DocumentBuilderFactory.newInstance()

        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)

        val builder = factory.newDocumentBuilder()


        fun loadFromPom(pom: URI): List<MavenDependency> {
            fun Node.stringValue(tag: String): String? = this[tag].firstOrNull()?.childNodes?.item(0)?.nodeValue

            val doc = builder.parse(pom.openStream()).documentElement

            val parent = doc["parent"].firstOrNull()?.let {
                MavenProject(
                    it.stringValue("groupId")!!,
                    it.stringValue("artifactId")!!,
                    it.stringValue("version")!!, // Kill me now if any of these are property substitutions... I can't deal with it...
                )
            }

            return doc["dependencies"].firstOrNull()?.get("dependency")?.map {
                val depGroup = it.stringValue("groupId")!!
                val depArtifact = it.stringValue("artifactId")!!

                fun loadProperty(name: String): String =
                    (doc["properties"].firstOrNull() ?: run {
                        if (parent == null) throw IllegalArgumentException("Failed to load property: $name")
                        builder.parse(loadArtifact(parent).pom.openStream()).documentElement["properties"].firstOrNull()
                            ?: throw IllegalArgumentException("Parent has no properties defined")
                    }).stringValue(name)
                        ?: throw IllegalStateException("Found properties but no definition for $name")

                fun loadIfAsProperty(value: String?) = if (value == null) null
                else if (value.startsWith("\${") && value.endsWith("}")) {
                    loadProperty(value.substring(2, value.length - 1))
                } else value

                MavenDependency(
                    loadIfAsProperty(depGroup)!!,
                    loadIfAsProperty(depArtifact)!!,
                    loadIfAsProperty(it.stringValue("version") ?: run {
                        val current =
                            "$repo/${depGroup.replace('.', '/')}/${it.stringValue("artifactId")}"

                        val meta = URI("$current/maven-metadata.xml")

                        assert(
                            meta.toURL().isReachable()
                        ) { "Failed to find maven central metadata for dependency \"$depGroup:$depArtifact\"" }

                        val docElem = builder.parse(meta.openStream()).documentElement

                        docElem.stringValue("version")
                            ?: docElem["versioning"].first().stringValue("release")
                            ?: throw IllegalStateException("Failed to find latest version for dependency \"$depGroup:$depArtifact\"")
                    })!!,
                    loadIfAsProperty(it.stringValue("scope")) ?: DEFAULT_SCOPE
                )
            } ?: ArrayList()
        }


        val dependencies = loadFromPom(pom)

        val needed = dependencies.filter {
            when (it.scope) {
                "compile", "provided", "runtime" -> true
                else -> false
            }
        } + dependencies.filter { it.scope == "import" }.map(::loadArtifact).map(MavenArtifact::pom)
            .flatMap(::loadFromPom)



        return Dependency(jar, needed.map { "${it.group}:${it.artifact}:${it.version}" }.mapNotNull(this::find))
    }


}