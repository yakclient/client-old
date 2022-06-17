package net.yakclient.client.boot.maven.pom

import net.yakclient.client.boot.maven.plugin.MockMavenPlugin

internal class SecondaryInterpolationStage : PomProcessStage<PluginLoadingStage.PluginLoadingData, SecondaryInterpolationStage.SecondaryInterpolationData> {
    override fun process(i: PluginLoadingStage.PluginLoadingData): SecondaryInterpolationData {
        val (data, _, mockPlugins: List<MockMavenPlugin>, parents) = i

        return PropertyReplacer.of(data, parents, *mockPlugins.toTypedArray()) {
            val newData = doInterpolation(data)

            SecondaryInterpolationData(newData)
        }
    }

    data class SecondaryInterpolationData(
        val data: PomData,
    ) : PomProcessStage.StageData
}