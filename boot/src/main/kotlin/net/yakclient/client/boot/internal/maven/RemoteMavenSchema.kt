package net.yakclient.client.boot.internal.maven

import net.yakclient.client.util.*
import java.net.URL

private val CENTRAL: URL = URL("https://repo.maven.apache.org/maven2")

internal object RemoteMavenSchema : MavenSchema() {
    override val versionedArtifact by createScheme(MavenSchemeContext::versionedArtifact)
    override val artifact by createScheme(MavenSchemeContext::baseArtifact)
    override val jar by createScheme {
        val s = "${it.project.artifact}-${it.project.version}"
        val a = it.versionedArtifact
        a?.uriAt("$s.jar")?.toResource(a.uriAt("$s.jar.md5").readHexToBytes())
    }
    override val meta by createScheme {
        val b = it.baseArtifact
        b.uriAt("maven-metadata.xml").toResource(b.uriAt("maven-metadata.xml.md5").readHexToBytes())
    }
    override val pom by createScheme {
        val s = "${it.project.artifact}-${it.project.version}"
        val v = it.versionedArtifact
        v?.uriAt("$s.pom")?.toResource(v.uriAt("$s.pom.md5").readHexToBytes())
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