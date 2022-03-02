package net.yakclient.client.boot.internal.maven

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class MavenDependency(
    var groupId: String,
    var artifactId: String,
    var version: String?,
    var scope: String?,
) {
    fun toDescriptor(): MavenDescriptor = MavenDescriptor(groupId, artifactId, version)
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class PomParent(
    var groupId: String,
    var artifactId: String,
    var version: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Pom(
    var groupId: String?,
    var artifactId: String,
    var version: String?,
    var properties: Map<String, String>?,
    var parent: PomParent?,
    val dependencies: List<MavenDependency>?
)