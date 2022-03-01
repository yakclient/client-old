package net.yakclient.client.boot.dep

import java.nio.file.Path
import kotlin.reflect.full.isSuperclassOf


internal data class CachedDependency(
    val path: Path,
    val dependants: List<Descriptor>,
    val desc: Descriptor
) {
    internal data class Descriptor(
        override val artifact: String,
        override val version: String?
    ) : Dependency.Descriptor {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || !Dependency.Descriptor::class.isSuperclassOf(other::class)) return false

            other as Dependency.Descriptor

            if (artifact != other.artifact) return false
//            if (version != other.version) return false

            return true
        }

        override fun hashCode(): Int = //            result = 31 * result + (version?.hashCode() ?: 0)
            artifact.hashCode()
    }
}