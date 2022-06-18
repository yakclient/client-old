package net.yakclient.client.boot.dependency

import java.nio.file.Path
import kotlin.reflect.full.isSuperclassOf


public data class CachedDependency(
    val path: Path?,
    val dependants: List<Descriptor>,
    val desc: Descriptor
) {
    public data class Descriptor(
        override val artifact: String,
        override val version: String?,
        override val classifier: String?
    ) : Dependency.Descriptor {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Descriptor) return false

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