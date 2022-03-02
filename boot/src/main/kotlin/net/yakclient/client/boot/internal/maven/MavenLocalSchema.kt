package net.yakclient.client.boot.internal.maven

import net.yakclient.client.util.toResource
import net.yakclient.client.util.toUrl
import java.nio.file.Path

private val LOCAL = Path.of(System.getProperty("user.home")).resolve(".m2").resolve("repository")

internal object MavenLocalSchema : MavenSchema() {
    override val meta by createScheme {
        val b = it.baseArtifact
        b.resolve("maven-metadata-local.xml").toResource()
    }
    override val versionedArtifact by createScheme {
        it.versionedArtifact?.toUrl()
    }
    override val artifact by createScheme {
        it.baseArtifact.toUrl()
    }
    override val jar by createScheme {
        it.versionedArtifact?.resolve("${it.project.artifact}-${it.project.version}.jar")?.toResource()
    }
    override val pom by createScheme {
        it.versionedArtifact?.resolve("${it.project.artifact}-${it.project.version}.pom")?.toResource()
    }
}

private val MavenSchemeContext.baseArtifact: Path
    get() = LOCAL.resolve(project.group.replace('.', '/')).resolve(project.artifact)

private val MavenSchemeContext.versionedArtifact: Path?
    get() = project.version?.let { baseArtifact.resolve(it) }