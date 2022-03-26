package net.yakclient.client.boot.internal

import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.resource.SafeResource
import net.yakclient.client.util.toResource
import java.nio.file.Files
import java.nio.file.Path

private val LOCAL = Path.of(System.getProperty("user.home")).resolve(".m2").resolve("repository")
private val path = LOCAL.toAbsolutePath().toString()

internal object LocalMavenLayout : MavenRepositoryLayout {
    override val settings: RepositorySettings = RepositorySettings(
        path, mapOf(
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

    override fun jarOf(g: String, a: String, v: String): SafeResource? =
        versionedArtifact(g, a, v).resolve("${a}-${v}.jar").takeIf(Files::exists)?.toResource()
}

private fun baseArtifact(group: String, artifact: String): Path =
    LOCAL.resolve(group.replace('.', '/')).resolve(artifact)

private fun versionedArtifact(g: String, a: String, v: String): Path = baseArtifact(g, a).resolve(v)

//private val LOCAL = Path.of(System.getProperty("user.home")).resolve(".m2").resolve("repository")
//
//internal object MavenLocalSchema : MavenSchema {
//    override val handler: SchemaHandler<MavenArtifactContext> = SchemaHandler()
//    init {
//        handler.registerValidator<MavenArtifactContext> {
//            Files.exists(it.baseArtifact)
//        }
//        handler.registerValidator<MavenVersionContext> {
//            Files.exists(it.versionedArtifact)
//        }
//    }
//
//    override val meta = handler.register(MavenArtifactContext::class) {
//        val b = it.baseArtifact
//        b.resolve("maven-metadata-local.xml").toResource()
//    }
//    override val jar : SchemaMeta<MavenVersionContext, SafeResource?> = handler.register(MavenVersionContext::class) {
//        it.versionedArtifact.resolve("${it.artifact}-${it.version}.jar").toResource()
//    }
//    override val pom = handler.register(MavenVersionContext::class) {
//        it.versionedArtifact.resolve("${it.artifact}-${it.version}.pom").toResource()
//    }
//}