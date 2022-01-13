package net.yakclient.client.boot.repository

public data class ArtifactID(
    val group: String,
    val name: String,
    val version: String
) {
    override fun toString(): String = "$group:$name:$version"
}
