package net.yakclient.client.boot.maven.layout

import net.yakclient.client.boot.maven.url
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.common.util.*
import net.yakclient.common.util.resource.SafeResource
import java.net.URL

public open class DefaultMavenLayout(override val settings: RepositorySettings) : MavenRepositoryLayout {
    private val url: String
        get() = settings.url ?: throw IllegalArgumentException("Invalid url: null")

    override fun artifactOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : SafeResource? {
        return versionedArtifact(groupId, artifactId, version).resourceAt("${artifactId}-${version}${classifier?.let { "-$it" } ?: ""}.$type")
    }

    protected fun URL.resourceAt(resource: String, checksumEnding: String = ".sha1"): SafeResource? =
        uriAt(resource).takeIf { it.toURL().isReachable() }?.toResource(uriAt("$resource$checksumEnding").readAsSha1())

    protected fun baseArtifact(g: String, a: String): URL =
        URL(url).urlAt(g.replace('.', '/'), a)

    protected fun versionedArtifact(g: String, a: String, v: String): URL = baseArtifact(g, a).urlAt(v)

}