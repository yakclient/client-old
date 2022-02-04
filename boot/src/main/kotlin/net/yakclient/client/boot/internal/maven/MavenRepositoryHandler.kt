package net.yakclient.client.boot.internal.maven

import net.yakclient.client.boot.internal.maven.MavenArtifact.Companion.loadArtifact
import net.yakclient.client.boot.internal.maven.property.*
import net.yakclient.client.boot.internal.maven.property.PomPropertyProvider
import net.yakclient.client.boot.dep.Dependency
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType
import net.yakclient.client.util.get
import net.yakclient.client.util.isReachable
import net.yakclient.client.util.openStream
import net.yakclient.client.util.valueOf
import org.w3c.dom.Element
import java.net.URI
import java.util.logging.Level
import java.util.logging.Logger
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

private const val DEFAULT_SCOPE = "runtime"

public class MavenRepositoryHandler(override val settings: RepositorySettings) : RepositoryHandler<MavenDescriptor> {
    private val logger = Logger.getLogger(this::class.simpleName)
    private val repo = when (settings.type) {
        RepositoryType.MAVEN -> settings.path
            ?: throw IllegalArgumentException("No repository provided! Please include a path in your repository declaration(or declare MAVEN_CENTRAL as the type)")
        RepositoryType.MAVEN_CENTRAL -> "https://repo.maven.apache.org/maven2"
        else -> throw IllegalArgumentException("Unknown repo type: ${settings.type}")
    }
    private val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance().also {
        it.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    }


    private val propertyProviders = listOf(
        PomPropertyProvider(factory, repo),
        PomVersionProvider(factory, repo)
    )

    private fun loadProperty(doc: Element, property: String) =
        propertyProviders.firstNotNullOfOrNull { it.provide(doc, property) }

    override fun find(desc: MavenDescriptor): Dependency? {
        val project = MavenProject(desc.group, desc.artifact, desc.version)

        logger.log(Level.FINEST, "Loading maven dependency: '$project'")

        val (artifact, jar, pom) = loadArtifact(repo, project)

        if (!artifact.toURL().isReachable()) {
            logger.log(
                Level.WARNING,
                "Failed to find dependency: '$desc' in maven repository: '$repo' at url: $artifact"
            )
            return null
        }

        fun loadPomDependencies(pom: URI): List<MavenDependency> {
            val doc = factory.newDocumentBuilder().parse(pom.openStream()).documentElement

            return doc["dependencies"].firstOrNull()?.get("dependency")?.map {
                val depGroup = it.valueOf("groupId")!!
                val depArtifact = it.valueOf("artifactId")!!

                fun loadProperty(name: String) = loadProperty(doc, name)

                fun loadIfAsProperty(value: String?): String? =
                    if (value == null) null
                    else if (value.startsWith("\${") && value.endsWith("}")) {
                        loadIfAsProperty(loadProperty(value.substring(2, value.length - 1)))
                    } else value

                MavenDependency(
                    loadIfAsProperty(depGroup)!!,
                    loadIfAsProperty(depArtifact)!!,
                    loadIfAsProperty(it.valueOf("version")) ?: run {
                        val current =
                            "$repo/${depGroup.replace('.', '/')}/${it.valueOf("artifactId")}"

                        val meta = URI("$current/maven-metadata.xml")

                        assert(
                            meta.toURL().isReachable()
                        ) { "Failed to find maven central metadata for dependency \"$depGroup:$depArtifact\"" }

                        val docElem = factory.newDocumentBuilder().parse(meta.openStream()).documentElement

                        docElem.valueOf("version")
                            ?: docElem["versioning"].first().valueOf("release")
                            ?: throw IllegalStateException("Failed to find latest version for dependency \"$depGroup:$depArtifact\"")
                    },
                    loadIfAsProperty(it.valueOf("scope")) ?: DEFAULT_SCOPE
                )
            } ?: ArrayList()
        }

        val dependencies = loadPomDependencies(pom)

        val needed = dependencies.filter {
            when (it.scope) {
                "compile", "provided", "runtime" -> true
                else -> false
            }
        } + dependencies.filter { it.scope == "import" }.map { loadArtifact(repo, it) }.map(MavenArtifact::pom)
            .flatMap(::loadPomDependencies)

        return Dependency(
            jar,
            needed.map { MavenDescriptor(it.group, it.artifact, it.version) },
            desc
        )
    }

    override fun loadDescription(dep: String): MavenDescriptor? =
        dep.split(':').takeIf { it.size == 3 }?.let { MavenDescriptor(it[0], it[1], it[2]) }
}