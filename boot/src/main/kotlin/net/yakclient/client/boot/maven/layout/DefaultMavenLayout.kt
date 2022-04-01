package net.yakclient.client.boot.maven.layout

import net.yakclient.client.boot.maven.layout
import net.yakclient.client.boot.maven.url
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.*
import net.yakclient.client.util.resource.SafeResource
import java.net.URL

public open class DefaultMavenLayout(override val settings: RepositorySettings) : MavenRepositoryLayout {
    private val url: String
        get() = settings.url ?: throw IllegalArgumentException("Invalid url: null")


    override fun artifactMetaOf(g: String, a: String): SafeResource {
        val b = baseArtifact(g, a)
        return b.resourceAt("maven-metadata.xml") ?: throw InvalidMavenLayoutException(
            "maven-metadata.xml",
            settings.layout
        )
//        return b.uriAt("maven-metadata.xml").takeIf { it.toURL().isReachable() }
//            ?.toResource(b.uriAt("maven-metadata.xml.sha1").readHex())
//            ?: throw InvalidMavenLayoutException("maven-metadata.xml", settings.layout)
    }

    override fun pomOf(g: String, a: String, v: String): SafeResource {
        val s = "${a}-${v}"

        return versionedArtifact(g, a, v).resourceAt("$s.pom") ?: throw InvalidMavenLayoutException(
            "$s.pom",
            settings.layout
        )
    }

    override fun archiveOf(g: String, a: String, v: String): SafeResource =
        versionedArtifact(g, a, v).resourceAt("${a}-${v}.jar") ?: throw InvalidMavenLayoutException("${a}-${v}.jar", settings.layout)

    protected fun URL.resourceAt(resource: String, checksumEnding: String = ".sha1"): SafeResource? =
        uriAt(resource).takeIf { it.toURL().isReachable() }?.toResource(uriAt("$resource$checksumEnding").readAsSha1())

    protected fun baseArtifact(g: String, a: String): URL =
        URL(url).urlAt(g.replace('.', '/'), a)

    protected fun versionedArtifact(g: String, a: String, v: String): URL = baseArtifact(g, a).urlAt(v)
}