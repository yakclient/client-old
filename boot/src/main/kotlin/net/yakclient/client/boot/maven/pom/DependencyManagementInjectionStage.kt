package net.yakclient.client.boot.maven.pom

import net.yakclient.client.boot.maven.layout.MavenLayoutFactory
import net.yakclient.client.boot.maven.parseData
import net.yakclient.client.boot.maven.url

internal class DependencyManagementInjectionStage :
    PomProcessStage<SecondaryInterpolationStage.SecondaryInterpolationData, DependencyManagementInjectionStage.DependencyManagementInjectionData> {
    override fun process(i: SecondaryInterpolationStage.SecondaryInterpolationData): DependencyManagementInjectionData {
        val (data, repo) = i

        val layouts = listOf(repo.layout) + data.repositories
            .map(PomRepository::toSettings)
            .map(MavenLayoutFactory::createLayout)

        val boms = data.dependencyManagement.dependencies.filter { it.scope == "import" }
            .map { bom ->
                parseData(
                    layouts.firstNotNullOfOrNull { l ->
                        l.artifactOf(
                            bom.groupId,
                            bom.artifactId,
                            bom.version,
                            null,
                            "pom"
                        )
                    } ?: throw IllegalStateException(
                        "Failed to find BOM $'${bom.groupId}:${bom.artifactId}:${bom.version}' in repositories: ${layouts.joinToString { it.settings.url ?: it.settings.type }}"
                    )
                )
            }

        val managedDependencies =
            data.dependencyManagement.dependencies.filterNot { it.scope == "import" } + boms.flatMap { it.dependencyManagement.dependencies }

        val dependencies = data.dependencies.mapTo(HashSet()) { dep ->
            val managed by lazy { managedDependencies.find { dep.groupId == it.groupId && dep.artifactId == it.artifactId } }

            MavenDependency(
                dep.groupId,
                dep.artifactId,
                dep.version ?: managed?.version
                ?: throw IllegalArgumentException("Failed to determine version for dependency: $'${dep.groupId}:${dep.artifactId}'"),
                dep.classifier ?: managed?.classifier,
                dep.scope ?: managed?.scope
            )
        }

        val newData = data.copy(
            dependencies = dependencies
        )

        return DependencyManagementInjectionData(newData)
    }

    data class DependencyManagementInjectionData(
        val data: PomData
    ) : PomProcessStage.StageData
}