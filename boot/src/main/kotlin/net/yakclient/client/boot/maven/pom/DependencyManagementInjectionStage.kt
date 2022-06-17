package net.yakclient.client.boot.maven.pom

internal class DependencyManagementInjectionStage :
    PomProcessStage<SecondaryInterpolationStage.SecondaryInterpolationData, DependencyManagementInjectionStage.DependencyManagementInjectionData> {
    override fun process(i: SecondaryInterpolationStage.SecondaryInterpolationData): DependencyManagementInjectionData {
        val (data) = i

        val dependencies = data.dependencies.mapTo(HashSet()) { dep ->
            val managed by lazy { data.dependencyManagement.dependencies.find { dep.groupId == it.groupId && dep.artifactId == it.artifactId } }

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