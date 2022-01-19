package net.yakclient.client.boot.internal.maven

import java.net.URI

internal data class MavenArtifact(
    val artifact: URI,
    val jar: URI,
    val pom: URI
) {
    companion object {
        fun loadArtifact(repo: String, proj: MavenProject): MavenArtifact =
            "$repo/${proj.group.replace('.', '/')}/${proj.artifact}".let {
                MavenArtifact(
                    URI(it),
                    URI("$it/${proj.version}/${proj.artifact}-${proj.version}.jar"),
                    URI("$it/${proj.version}/${proj.artifact}-${proj.version}.pom")
                )
            }
    }
}