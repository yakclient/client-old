package net.yakclient.client.boot.maven

import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.*
import net.yakclient.client.util.resource.SafeResource
import java.net.URL

public open class RemoteMavenHandler(settings: RepositorySettings) : MavenRepositoryHandler(settings) {
    override fun metaOf(group: String, artifact: String): SafeResource? {
        val b = baseArtifact(group, artifact)
        return b.uriAt("maven-metadata.xml").takeIf { it.toURL().isReachable() }
            ?.toResource(b.uriAt("maven-metadata.xml.sha1").readHexToBytes())
    }

    override fun pomOf(desc: MavenDescriptor): SafeResource? {
        if (desc.version == null) return null

        val s = "${desc.artifact}-${desc.version}"
        val v = versionedArtifact(desc) ?: return null
        return v.uriAt("$s.pom").takeIf { it.toURL().isReachable() }
            ?.toResource(v.uriAt("$s.pom.sha1").readHexToBytes())
    }

    override fun jarOf(desc: MavenDescriptor): SafeResource? {
        if (desc.version == null) return null

        val s = "${desc.artifact}-${desc.version}"
        val a = versionedArtifact(desc) ?: return null
        return a.uriAt("$s.jar").takeIf { it.toURL().isReachable() }
            ?.toResource(a.uriAt("$s.jar.sha1").readHexToBytes())
    }

    private fun baseArtifact(group: String, artifact: String): URL =
        URL(settings.url).urlAt(group.replace('.', '/'), artifact)

    private fun versionedArtifact(c: MavenDescriptor): URL? =
        c.let { baseArtifact(c.group, c.artifact).urlAt(it.version ?: return@let null) }
}