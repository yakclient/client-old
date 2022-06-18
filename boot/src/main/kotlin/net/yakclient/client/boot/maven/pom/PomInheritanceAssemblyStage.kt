package net.yakclient.client.boot.maven.pom

import com.fasterxml.jackson.databind.ObjectReader
import net.yakclient.client.boot.internal.CentralMavenLayout
import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.maven.layout.MavenRepositoryLayout
import net.yakclient.client.boot.maven.mavenCentral

internal class PomInheritanceAssemblyStage :
    PomProcessStage<ParentResolutionStage.ParentResolutionData, PomInheritanceAssemblyStage.AssembledPomData> {
    // TODO Redo the pom inheritance, its technically not fully correct since list fields dont get added together correctly.
    override fun process(i: ParentResolutionStage.ParentResolutionData): AssembledPomData {
        val (data, ref, parents) = i

        val all = listOf(data) + parents
        fun <T> travelUntil(req: (PomData) -> T?): T =
            all.firstNotNullOfOrNull(req) ?: throw IllegalArgumentException("Failed to find value in all poms!")

        val groupId = travelUntil(PomData::groupId)
        val artifactId = data.artifactId
        val version = travelUntil(PomData::version)

        val properties = all.fold(mapOf()) { acc: Map<String, String>, it -> acc + it.properties }
        val parent = data.parent
        val dependencyManagement =
            DependencyManagement(all.flatMapTo(HashSet()) { it.dependencyManagement.dependencies })

        val dependencies = all.flatMapTo(HashSet(), PomData::dependencies)
        val repositories = all.flatMap(PomData::repositories)

        val plugins = all.flatMap { it.build.plugins }
        val extensions = all.flatMap { it.build.extensions }
        val pluginManagement = PomPluginManagement(all.flatMap { it.build.pluginManagement.plugins })

        val build = PomBuild(extensions, plugins, pluginManagement)

        val packaging = data.packaging

        val assembledData = PomData(
            groupId,
            artifactId,
            version,
            properties,
            parent,
            dependencyManagement,
            dependencies,
            repositories,
            build,
            packaging
        )

        return AssembledPomData(assembledData, parents, ref)
    }

    data class AssembledPomData(
        val pomData: PomData,
        val parents: List<PomData>,
        val thisRepo: MavenRepositoryHandler
    ) : PomProcessStage.StageData
}