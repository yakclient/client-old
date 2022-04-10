package net.yakclient.client.boot.internal

import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.maven.layout.InvalidMavenLayoutException
import net.yakclient.client.boot.maven.layout.MavenRepositoryLayout
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.resource.SafeResource
import net.yakclient.client.util.toResource
import java.nio.file.Files
import java.nio.file.Path

private val LOCAL = Path.of(System.getProperty("user.home")).resolve(".m2").resolve("repository")
private val path = LOCAL.toAbsolutePath().toString()

internal object LocalMavenLayout : MavenRepositoryLayout {
    override val settings: RepositorySettings = RepositorySettings(
        MAVEN_LOCAL, mapOf(
            LAYOUT_OPTION_NAME to "local",
            URL_OPTION_NAME to path
        )
    )

    override fun artifactMetaOf(g: String, a: String): SafeResource =
        (baseArtifact(g, a)).resolve("maven-metadata-local.xml").takeIf(Files::exists)?.toResource()
            ?: throw InvalidMavenLayoutException("maven-metadata-local.xml", "local")

    override fun pomOf(g: String, a: String, v: String): SafeResource =
        versionedArtifact(g, a, v).resolve("${a}-${v}.pom").takeIf(Files::exists)?.toResource()
            ?: throw InvalidMavenLayoutException("${a}-${v}.pom", "local")

    override fun archiveOf(g: String, a: String, v: String): SafeResource =
        versionedArtifact(g, a, v).resolve("${a}-${v}.jar").takeIf(Files::exists)?.toResource() ?: throw InvalidMavenLayoutException("${a}-${v}.jar", "local")
}

private fun baseArtifact(group: String, artifact: String): Path =
    LOCAL.resolve(group.replace('.', '/')).resolve(artifact)

private fun versionedArtifact(g: String, a: String, v: String): Path = baseArtifact(g, a).resolve(v)