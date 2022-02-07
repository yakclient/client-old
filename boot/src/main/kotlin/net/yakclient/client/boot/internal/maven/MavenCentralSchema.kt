package net.yakclient.client.boot.internal.maven

import net.yakclient.client.util.resourceAt
import net.yakclient.client.util.urlAt
import java.net.URL

private val CENTRAL: URL = URL("https://repo.maven.apache.org/maven2")

internal object MavenCentralSchema : MavenSchema() {
    override val versionedArtifact by createScheme(MavenSchemeContext::versionedArtifact)
    override val artifact by createScheme(MavenSchemeContext::baseArtifact)
    override val jar by createScheme {
        it.versionedArtifact?.resourceAt("${it.project.artifact}-${it.project.version}.jar")
    }
    override val meta by createScheme {
        it.baseArtifact.resourceAt("maven-metadata.xml")
    }
    override val pom by createScheme {
        it.versionedArtifact?.resourceAt("${it.project.artifact}-${it.project.version}.pom")
    }
}

private val MavenSchemeContext.baseArtifact: URL
    get() = CENTRAL.urlAt(project.group.replace('.', '/'), project.artifact)

private val MavenSchemeContext.versionedArtifact: URL?
    get() = project.version?.let { baseArtifact.urlAt(it) }

//internal class MavenCentralSchemeContext(
//    repo: URL,
//    project: MavenProject
//) : MavenSchemeContext(project) {
//    val baseArtifact: URL = repo.urlAt(project.group.replace('.', '/'), project.artifact)
//    val versionedArtifact: URL = baseArtifact.urlAt(project.version)
//}