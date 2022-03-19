package net.yakclient.client.boot.maven

import net.yakclient.client.util.SchemaHandler
import net.yakclient.client.util.*
import net.yakclient.client.util.resource.SafeResource
import java.net.URL

public open class RemoteMavenSchema(
    private val repo: URL
) : MavenSchema {
    public constructor(repo: String) : this(URL(repo))

    final override val handler: SchemaHandler<MavenArtifactContext>

    init {
        val handler = SchemaHandler<MavenArtifactContext>()

        handler.registerValidator<MavenArtifactContext> {
            val b = baseArtifact(it)
            b.uriAt("maven-metadata.xml").toURL().isReachable()
        }
        handler.registerValidator<MavenVersionContext> {
            val s = "${it.artifact}-${it.version}"
            val a = versionedArtifact(it)
            a.uriAt("$s.pom").toURL().isReachable() // Don't need the jar to be available
        }

        this.handler = handler
    }

    override val jar: SchemaMeta<MavenVersionContext, SafeResource?> = handler.register(MavenVersionContext::class) {
        val s = "${it.artifact}-${it.version}"
        val a = versionedArtifact(it)
        a.uriAt("$s.jar").takeIf { it.toURL().isReachable() }?.toResource(a.uriAt("$s.jar.sha1").readHexToBytes())
    }
    override val meta: SchemaMeta<MavenArtifactContext, SafeResource> = handler.register(MavenArtifactContext::class) {
        val b = baseArtifact(it)
        b.uriAt("maven-metadata.xml").toResource(b.uriAt("maven-metadata.xml.sha1").readHexToBytes())
    }
    override val pom: SchemaMeta<MavenVersionContext, SafeResource> = handler.register(MavenVersionContext::class) {
        val s = "${it.artifact}-${it.version}"
        val v = versionedArtifact(it)
        v.uriAt("$s.pom").toResource(v.uriAt("$s.pom.sha1").readHexToBytes())
    }

    private fun baseArtifact(c: MavenArtifactContext): URL =
        repo.urlAt(c.group.replace('.', '/'), c.artifact)

    private fun versionedArtifact(c: MavenVersionContext): URL = c.let { baseArtifact(c).urlAt(it.version) }
}