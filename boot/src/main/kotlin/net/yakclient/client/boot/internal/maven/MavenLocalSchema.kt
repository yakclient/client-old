package net.yakclient.client.boot.internal.maven

import net.yakclient.client.boot.schema.SchemaHandler
import net.yakclient.client.util.toResource
import java.nio.file.Files
import java.nio.file.Path

private val LOCAL = Path.of(System.getProperty("user.home")).resolve(".m2").resolve("repository")

internal object MavenLocalSchema : MavenSchema {
    override val handler: SchemaHandler<MavenArtifactContext> = SchemaHandler()
    init {
        handler.registerValidator<MavenArtifactContext> {
            Files.exists(it.baseArtifact)
        }
        handler.registerValidator<MavenVersionContext> {
            Files.exists(it.versionedArtifact)
        }
    }

    override val meta = handler.register(MavenArtifactContext::class) {
        val b = it.baseArtifact
        b.resolve("maven-metadata-local.xml").toResource()
    }
    override val jar = handler.register(MavenVersionContext::class) {
        it.versionedArtifact.resolve("${it.artifact}-${it.version}.jar").toResource()
    }
    override val pom = handler.register(MavenVersionContext::class) {
        it.versionedArtifact.resolve("${it.artifact}-${it.version}.pom").toResource()
    }
}

private val MavenArtifactContext.baseArtifact: Path
    get() = LOCAL.resolve(group.replace('.', '/')).resolve(artifact)

private val MavenVersionContext.versionedArtifact: Path
    get() = version.let { baseArtifact.resolve(it) }