package net.yakclient.client.boot.internal.maven

internal open class MavenProject(
    val group: String,
    val artifact: String,
    val version: String,
)

internal class MavenDependency(
    group: String, artifact: String, version: String,
    val scope: String,
) : MavenProject(group, artifact, version)

