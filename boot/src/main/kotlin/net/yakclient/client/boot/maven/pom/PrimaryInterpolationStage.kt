package net.yakclient.client.boot.maven.pom

import net.yakclient.client.boot.maven.MavenRepositoryHandler
import net.yakclient.client.boot.maven.layout.MavenRepositoryLayout
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

internal class PrimaryInterpolationStage :
    PomProcessStage<PomInheritanceAssemblyStage.AssembledPomData, PrimaryInterpolationStage.PrimaryInterpolationData> {
    override fun process(i: PomInheritanceAssemblyStage.AssembledPomData): PrimaryInterpolationData {
        val (data, parents, repo) = i

       return PropertyReplacer.of(data, parents) {
           val newData = doInterpolation(data)

            PrimaryInterpolationData(newData, parents, repo)
        }
    }

    data class PrimaryInterpolationData(
        val pomData: PomData,
        val parents: List<PomData>,
        val thisRepo: MavenRepositoryHandler
    ) : PomProcessStage.StageData

}