package net.yakclient.client.boot.maven

import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.*
import net.yakclient.client.util.resource.SafeResource
import java.net.URL

public open class DefaultMavenLayout(override val settings: RepositorySettings) : MavenRepositoryLayout {
    private val url : String
        get() = settings.url ?: throw IllegalArgumentException("Invalid url: null")


    override fun artifactMetaOf(g: String, a: String): SafeResource {
        val b = baseArtifact(g, a)
        return b.uriAt("maven-metadata.xml").takeIf { it.toURL().isReachable() }
            ?.toResource(b.uriAt("maven-metadata.xml.sha1").readHexToBytes())
            ?: throw InvalidMavenLayoutException("maven-metadata.xml", settings.layout)
    }

    override fun pomOf(g: String, a: String, v: String): SafeResource {
        val s = "${a}-${v}"
        val va = versionedArtifact(g, a, v)
        return va.uriAt("$s.pom").takeIf { it.toURL().isReachable() }
            ?.toResource(va.uriAt("$s.pom.sha1").readHexToBytes()) ?: throw InvalidMavenLayoutException(
            "$s.pom",
            settings.layout
        )
    }

    override fun jarOf(g: String, a: String, v: String): SafeResource? {
        val s = "${a}-${v}"
        val va = versionedArtifact(g, a, v)
        return va.uriAt("$s.jar").takeIf { it.toURL().isReachable() }
            ?.toResource(va.uriAt("$s.jar.sha1").readHexToBytes())
    }

    private fun baseArtifact(g: String, a: String): URL =
        URL(url).urlAt(g.replace('.', '/'), a)

    private fun versionedArtifact(g: String, a: String, v: String): URL = baseArtifact(g, a).urlAt(v)

}