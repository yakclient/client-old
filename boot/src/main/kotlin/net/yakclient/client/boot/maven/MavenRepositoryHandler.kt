package net.yakclient.client.boot.maven

import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.internal.maven.plugin.OsPlugin
import net.yakclient.client.boot.maven.layout.MavenRepositoryLayout
import net.yakclient.client.boot.maven.plugin.MockMavenPlugin
import net.yakclient.client.boot.maven.plugin.MockPluginConfiguration
import net.yakclient.client.boot.maven.plugin.MockPluginProvider
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.common.util.filterDuplicates
import java.util.logging.Level
import java.util.logging.Logger

public open class MavenRepositoryHandler(
    public val layout: MavenRepositoryLayout,
    override val settings: RepositorySettings,
    public val pluginProvider: MockPluginProvider = object : MockPluginProvider {
        private val plugins: Map<String, Map<MockMavenPlugin.VersionDescriptor, (config: MockPluginConfiguration) -> MockMavenPlugin>> =
            mapOf("${OsPlugin.MOCKED_GROUP}:${OsPlugin.MOCKED_ARTIFACT}" to mapOf(OsPlugin.MOCKED_VERSION to {
                OsPlugin(
                    it
                )
            }))

        override fun provide(
            group: String,
            artifact: String,
            version: MockMavenPlugin.VersionDescriptor,
            configuration: MockPluginConfiguration
        ): MockMavenPlugin? =
            plugins["$group:$artifact"]?.entries?.find { version.matches(it.key) }?.value?.invoke(configuration)
    }
) : RepositoryHandler<MavenDescriptor> {
    private val logger = Logger.getLogger(this::class.simpleName)

    override fun find(desc: MavenDescriptor): Dependency? =
        findInternal(desc)

    private fun findInternal(desc: MavenDescriptor): Dependency? {
        logger.log(Level.FINEST, "Loading maven dependency: '$desc'")

        val (group, artifact, version) = listOf(desc.group, desc.artifact, desc.version)

        val valueOr = layout.artifactOf(group, artifact, version, null, "pom")

        val pom = parsePom(valueOr ?: return null)

        val dependencies = pom.dependencies

        val repositories = pom.repositories.toMutableList().apply { add(settings) }.filterDuplicates()

        val needed = dependencies.filter {
            when (it.scope) {
                "compile", "runtime", "import" -> true
                else -> false
            }
        }.map {
            MavenDescriptor(
                it.groupId,
                it.artifactId,
                it.version,
                it.classifier
            )
        }

        return Dependency(
            if (pom.packaging != "pom") layout.artifactOf(
                group,
                artifact,
                version,
                desc.classifier,
                pom.packaging
            ) else null,
            needed.mapTo(HashSet()) {
                Dependency.Transitive(repositories, it)
            },
            desc
        )
    }

    override fun loadDescription(dep: String): MavenDescriptor? = MavenDescriptor.parseDescription(dep)
}