package net.yakclient.client.boot.internal

import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.resource.SafeResource
import net.yakclient.client.util.toResource
import java.nio.file.Files
import java.nio.file.Path

private val LOCAL = Path.of(System.getProperty("user.home")).resolve(".m2").resolve("repository")

internal object LocalMavenHandler : MavenRepositoryHandler(RepositorySettings(MAVEN_LOCAL, null)) {
    override fun metaOf(group: String, artifact: String): SafeResource? =
        (baseArtifact(group, artifact)).resolve("maven-metadata-local.xml").takeIf(Files::exists)?.toResource()

    override fun pomOf(desc: MavenDescriptor): SafeResource? =
        desc.versionedArtifact.resolve("${desc.artifact}-${desc.version}.pom").takeIf(Files::exists)?.toResource()

    override fun jarOf(desc: MavenDescriptor): SafeResource? =
        desc.versionedArtifact.resolve("${desc.artifact}-${desc.version}.jar").takeIf(Files::exists)?.toResource()
}

private fun baseArtifact(group: String, artifact: String): Path =
    LOCAL.resolve(group.replace('.', '/')).resolve(artifact)

private val MavenDescriptor.versionedArtifact: Path
    get() = (version
        ?: throw IllegalArgumentException("Version of descriptor: '$group:$artifact' must not be null!")).let {
        baseArtifact(
            group,
            artifact
        ).resolve(it)
    }

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