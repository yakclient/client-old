package net.yakclient.client.boot.internal.maven

import net.yakclient.client.boot.dependency.Dependency

public data class MavenDescriptor(
    val group: String,
    override val artifact: String,
   override val version: String?,
) : Dependency.Descriptor {
    override fun toString(): String = "$group:$artifact:$version"
}