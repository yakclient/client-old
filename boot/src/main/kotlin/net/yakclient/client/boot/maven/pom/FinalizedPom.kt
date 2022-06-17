package net.yakclient.client.boot.maven.pom

import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.repository.RepositorySettings

public data class FinalizedPom(
    val desc: Dependency.Descriptor,
    val repositories: List<RepositorySettings>,
    val dependencies: List<PomDependency>,
    val packaging: String
) : PomProcessStage.StageData

public data class PomDependency(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val classifier: String?,
    val scope: String,
)

internal fun mavenToPomDependency(dep: MavenDependency) = PomDependency(dep.groupId, dep.artifactId, dep.version!!, dep.classifier, dep.scope ?: "compile")