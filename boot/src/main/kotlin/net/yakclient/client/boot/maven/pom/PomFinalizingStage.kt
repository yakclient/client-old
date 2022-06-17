package net.yakclient.client.boot.maven.pom

import net.yakclient.client.boot.maven.MavenDescriptor

internal class PomFinalizingStage : PomProcessStage<DependencyManagementInjectionStage.DependencyManagementInjectionData, FinalizedPom> {
    override fun process(i: DependencyManagementInjectionStage.DependencyManagementInjectionData): FinalizedPom {
        val (data) = i

        return FinalizedPom(
            MavenDescriptor(
                data.groupId!!,
                data.artifactId,
                data.version!!,
                null
            ),
            data.repositories.map(PomRepository::toSettings),
            data.dependencies.map(::mavenToPomDependency),
            data.packaging
        )
    }
}

