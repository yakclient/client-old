package net.yakclient.client.boot.internal.maven

import net.yakclient.client.boot.schema.SchemaHandler
import net.yakclient.client.util.*
import java.net.URL

internal class RemoteMavenSchema(
    private val repo: URL
) : MavenSchema {
    constructor(repo: String) : this(URL(repo))

    override val handler: SchemaHandler<MavenArtifactContext> = SchemaHandler()

    init {
        handler.registerValidator<MavenArtifactContext> {
            baseArtifact(it).uriAt("maven-metadata.xml").toURL().isReachable()
        }
        handler.registerValidator<MavenVersionContext> {
            val s = "${it.artifact}-${it.version}"
            val a = versionedArtifact(it)
            a.uriAt("$s.pom").toURL().isReachable() && a.uriAt("$s.jar").toURL().isReachable()
        }
    }

    override val jar = handler.register(MavenVersionContext::class) {
        val s = "${it.artifact}-${it.version}"
        val a = versionedArtifact(it)
        a.uriAt("$s.jar").toResource(a.uriAt("$s.jar.sha1").readHexToBytes())
    }
    override val meta = handler.register(MavenArtifactContext::class) {
        val b = baseArtifact(it)
        b.uriAt("maven-metadata.xml").toResource(b.uriAt("maven-metadata.xml.sha1").readHexToBytes())
    }
    override val pom = handler.register(MavenVersionContext::class) {
        val s = "${it.artifact}-${it.version}"
        val v = versionedArtifact(it)
        v.uriAt("$s.pom").toResource(v.uriAt("$s.pom.sha1").readHexToBytes())
    }

    private fun baseArtifact(c: MavenArtifactContext): URL =
        repo.urlAt(c.group.replace('.', '/'), c.artifact)

    private fun versionedArtifact(c: MavenVersionContext): URL = c.let { baseArtifact(c).urlAt(it.version) }
}


//internal class MavenCentralSchemeContext(
//    repo: URL,
//    project: MavenProject
//) : MavenSchemeContext(project) {
//    val baseArtifact: URL = repo.urlAt(project.group.replace('.', '/'), project.artifact)
//    val versionedArtifact: URL = baseArtifact.urlAt(project.version)
//}