package net.yakclient.client.boot.maven.pom

import net.yakclient.client.boot.maven.MavenRepositoryHandler
import net.yakclient.client.boot.maven.plugin.MockMavenPlugin

internal class SecondaryInterpolationStage : PomProcessStage<PluginLoadingStage.PluginLoadingData, SecondaryInterpolationStage.SecondaryInterpolationData> {
    override fun process(i: PluginLoadingStage.PluginLoadingData): SecondaryInterpolationData {
        val (data, r, mockPlugins: List<MockMavenPlugin>, parents) = i

        return PropertyReplacer.of(data, parents, *mockPlugins.toTypedArray()) {
            val newData = doInterpolation(data)

            SecondaryInterpolationData(newData, r)
        }
    }

    data class SecondaryInterpolationData(
        val data: PomData,
//        val parents: List<PomData>,
        val thisRepo: MavenRepositoryHandler
    ) : PomProcessStage.StageData
}