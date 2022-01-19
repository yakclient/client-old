package net.yakclient.client.boot.internal.maven

import net.yakclient.client.boot.internal.maven.MavenArtifact.Companion.loadArtifact
import net.yakclient.client.boot.internal.maven.provider.*
import net.yakclient.client.boot.internal.maven.provider.LocalPomProvider
import net.yakclient.client.boot.internal.maven.provider.ParentPomProvider
import net.yakclient.client.boot.internal.maven.provider.ParentVersionProvider
import net.yakclient.client.boot.internal.maven.provider.ProjectVersionProvider
import net.yakclient.client.boot.repository.Dependency
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType
import net.yakclient.client.util.get
import net.yakclient.client.util.isReachable
import net.yakclient.client.util.openStream
import net.yakclient.client.util.valueOf
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.net.URI
import java.util.logging.Logger
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

private const val DEFAULT_SCOPE = "runtime"

public class MavenRepositoryHandler(override val settings: RepositorySettings) : RepositoryHandler {
    private val logger = Logger.getLogger(settings.name ?: "anonymous-maven-logger")
    private val repo = when (settings.type) {
        RepositoryType.MAVEN -> settings.path
            ?: throw IllegalArgumentException("No repository provided! Please include a path in your repository declaration(or declare MAVEN_CENTRAL as the type)")
        RepositoryType.MAVEN_CENTRAL -> "https://repo.maven.apache.org/maven2"
        else -> throw IllegalArgumentException("Unknown repo type: ${settings.type}")
    }
    private val builder = run {
        val factory = DocumentBuilderFactory.newInstance()

        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)

        factory.newDocumentBuilder()
    }

    private val propertyProviders = listOf(
        LocalPomProvider(),
        ParentPomProvider(builder, repo),
        ParentVersionProvider(builder, repo),
        ProjectVersionProvider(builder, repo)
    )

    private fun loadProperty(doc: Element, property: String) =
        propertyProviders.firstNotNullOfOrNull { it.provide(doc, property) }

    override fun find(it: String): Dependency? {
        val project = it.split(':').let { MavenProject(it[0], it[1], it[2]) }

        val (artifact, jar, pom) = loadArtifact(repo, project)
        println(pom)

        if (!artifact.toURL().isReachable()) return null



        fun loadFromPom(pom: URI): List<MavenDependency> {
            val doc = builder.parse(pom.openStream()).documentElement

            val parent = doc["parent"].firstOrNull()?.let {
                MavenProject(
                    it.valueOf("groupId")!!,
                    it.valueOf("artifactId")!!,
                    it.valueOf("version")!!, // Kill me now if any of these are property substitutions... I can't deal with it...
                )
            }

            return doc["dependencies"].firstOrNull()?.get("dependency")?.map {
                val depGroup = it.valueOf("groupId")!!
                val depArtifact = it.valueOf("artifactId")!!

//                fun loadProperty(name: String): String =
//                    (doc["properties"].firstOrNull() ?: run {
//                        if (parent == null) throw IllegalArgumentException("Failed to load property: $name")
//                        builder.parse(loadArtifact(parent).pom.openStream()).documentElement["properties"].firstOrNull()
//                            ?: throw IllegalArgumentException("Parent has no properties defined")
//                    }).valueOf(name)
//                        ?: throw IllegalStateException("Found properties but no definition for $name")

                fun loadProperty(name: String) = loadProperty(doc, name)

                fun loadIfAsProperty(value: String?) = if (value == null) null
                else if (value.startsWith("\${") && value.endsWith("}")) {
                    loadProperty(value.substring(2, value.length - 1))
                } else value

                MavenDependency(
                    loadIfAsProperty(depGroup)!!,
                    loadIfAsProperty(depArtifact)!!,
                    loadIfAsProperty(it.valueOf("version") ?: run {
                        val current =
                            "$repo/${depGroup.replace('.', '/')}/${it.valueOf("artifactId")}"

                        val meta = URI("$current/maven-metadata.xml")

                        assert(
                            meta.toURL().isReachable()
                        ) { "Failed to find maven central metadata for dependency \"$depGroup:$depArtifact\"" }

                        val docElem = builder.parse(meta.openStream()).documentElement

                        docElem.valueOf("version")
                            ?: docElem["versioning"].first().valueOf("release")
                            ?: throw IllegalStateException("Failed to find latest version for dependency \"$depGroup:$depArtifact\"")
                    })!!,
                    loadIfAsProperty(it.valueOf("scope")) ?: DEFAULT_SCOPE
                )
            } ?: ArrayList()
        }


        val dependencies = loadFromPom(pom)

        val needed = dependencies.filter {
            when (it.scope) {
                "compile", "provided", "runtime" -> true
                else -> false
            }
        } + dependencies.filter { it.scope == "import" }.map { loadArtifact(repo, it) }.map(MavenArtifact::pom)
            .flatMap(::loadFromPom)



        return Dependency(jar, needed.map { "${it.group}:${it.artifact}:${it.version}" }.mapNotNull(this::find))
    }


}