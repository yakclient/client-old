package net.yakclient.client.boot.maven.pom

import net.yakclient.client.boot.maven.MavenRepositoryHandler
import net.yakclient.client.boot.maven.plugin.MockMavenPlugin
import net.yakclient.client.boot.maven.plugin.MockPluginConfiguration

// Maven extensions are also considered plugins
internal class PluginLoadingStage :
    PomProcessStage<PluginManagementInjectionStage.PluginManagementInjectionData, PluginLoadingStage.PluginLoadingData> {
    override fun process(i: PluginManagementInjectionStage.PluginManagementInjectionData): PluginLoadingData {
        val (data, parents, repo) = i

        val plugins = data.build.plugins
        val extensions = data.build.extensions

        val mockPlugins = plugins.mapNotNull {
            repo.pluginProvider.provide(
                it.groupId,
                it.artifactId,
                MockMavenPlugin.VersionDescriptor(it.version),
                MockPluginConfiguration(it.configurations, data)
            )
        } + extensions.mapNotNull {
            repo.pluginProvider.provide(
                it.groupId,
                it.artifactId,
                MockMavenPlugin.VersionDescriptor(it.version),
                MockPluginConfiguration(it.configurations, data)
            )
        }

        return PluginLoadingData(data, repo, mockPlugins, parents)
    }

    data class PluginLoadingData(
        val data: PomData,
        val thisRepo: MavenRepositoryHandler,
        val plugins: List<MockMavenPlugin>,
        val parents: List<PomData>
    ) : PomProcessStage.StageData
}