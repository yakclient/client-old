package net.yakclient.client.boot.maven

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.runCatching
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger

internal open class MavenRepositoryHandler(
    private val layout: MavenRepositoryLayout,
    override val settings: RepositorySettings,
) : RepositoryHandler<MavenDescriptor> {
    private val logger = Logger.getLogger(this::class.simpleName)
    private val propertyMatcher = Regex("^\\$\\{(.*)}$")
    private val xml: ObjectMapper = XmlMapper().registerModule(KotlinModule())

    override fun find(desc: MavenDescriptor): Dependency? =
        findInternal(desc)

    protected open fun newestVersionOf(group: String, artifact: String): MavenDescriptor? {
        val meta =
            runCatching(InvalidMavenLayoutException::class) { layout.artifactMetaOf(group, artifact) } ?: return null

        val tree = xml.readValue<Map<String, Any>>(meta.open())

        val version = (tree["version"] as? String)
            ?: (tree["versioning"] as Map<String, String>)["release"]
            ?: return null

        return MavenDescriptor(group, artifact, version)
    }

    private fun findInternal(_desc: MavenDescriptor): Dependency? {
        val desc = if (_desc.version == null) newestVersionOf(_desc.group, _desc.artifact) ?: return null else _desc
        logger.log(Level.FINEST, "Loading maven dependency: '$desc'")

        val (group, artifact, version) = listOf(desc.group, desc.artifact, desc.version!!)

        val pom =
            loadMavenPom(runCatching(InvalidMavenLayoutException::class) { layout.pomOf(group, artifact, version) }
                ?: return null)

        fun getConst(name: String): String? =
            when (name) {
                "project.version" -> pom.desc.version
                "project.parent.version" -> pom.parent?.desc?.version
                else -> null
            }


        fun String.asIfProperty(): String {
            val match = propertyMatcher.matchEntire(this) ?: return this
            val name = match.groupValues[1]
            return (pom.findProperty(name) ?: getConst(name))?.asIfProperty() ?: throw IllegalArgumentException("Invalid property value: $this")
        }

        val dependencies = pom.dependencies

        val needed = dependencies.filter {
            when (it.scope) {
                "compile", "provided", "runtime", "import" -> true
                else -> false
            }
        }.map {
            MavenDescriptor(
                it.groupId.asIfProperty(),
                it.artifactId.asIfProperty(),
                it.version?.asIfProperty()
            )
        }

        val contains = HashSet<RepositorySettings>()

        val repositories = pom.repositories.filter(contains::add)

        return Dependency(
            layout.jarOf(group, artifact, version),
            needed.mapTo(HashSet()) {
                Dependency.Transitive(repositories, it)
            },
            desc
        )
    }

    override fun loadDescription(dep: String): MavenDescriptor? =
        dep.split(':').takeIf { it.size == 3 || it.size == 2 }?.let { MavenDescriptor(it[0], it[1], it.getOrNull(2)) }
}