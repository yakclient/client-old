package net.yakclient.client.boot.dependency

import java.nio.file.Path
import kotlin.reflect.full.isSuperclassOf


internal data class CachedDependency(
    val path: Path?,
    val dependants: List<Descriptor>,
    val desc: Descriptor
) {
    internal data class Descriptor(
        override val artifact: String,
        override val version: String?
    ) : Dependency.Descriptor
}