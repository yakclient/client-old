package net.yakclient.client.boot.maven

import net.yakclient.client.boot.dependency.Dependency

public data class MavenDescriptor(
    val group: String,
    override val artifact: String,
    override val version: String,
    override val classifier: String?
) : Dependency.Descriptor {
    override fun toString(): String = toPrettyString()

    override fun toPrettyString(): String ="$group:$artifact:$version${classifier?.let { ":$it" } ?: ""}"

    public companion object {
        public fun parseDescription(name: String): MavenDescriptor? = name.split(':').takeIf { it.size == 3 || it.size == 4 }?.let { MavenDescriptor(it[0], it[1], it[2], it.getOrNull(3)) }
    }
}