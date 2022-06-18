package net.yakclient.client.boot.dependency

import java.nio.file.Path


public data class CachedDependency(
    val path: Path?,
    val dependants: List<CachedDescriptor>,
    val desc: CachedDescriptor
) {
    public data class CachedDescriptor(
        override val artifact: String,
        override val version: String?,
        override val classifier: String?
    ) : Dependency.Descriptor {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CachedDescriptor) return false

            if (artifact != other.artifact) return false
            if (classifier != other.classifier) return false

            return true
        }

        override fun hashCode(): Int {
            var result = artifact.hashCode()
            result = 31 * result + (classifier?.hashCode() ?: 0)
            return result
        }
    }
}