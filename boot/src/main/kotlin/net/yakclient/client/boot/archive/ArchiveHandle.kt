package net.yakclient.client.boot.archive

import net.yakclient.client.util.resource.ProvidedResource
import net.yakclient.client.util.resource.SafeResource
import java.io.Closeable
import java.io.InputStream
import java.net.URI

public interface ArchiveHandle : Closeable {
    public val location: URI
    public val reader: Reader
    public val writer: Writer
    public val modified: Boolean
    public val isClosed: Boolean
    public val isOpen: Boolean
        get() = !isClosed

    public interface Reader {
        public fun of(name: String): Entry?

        public fun contains(name: String): Boolean = get(name) != null

        public fun entries(): Sequence<Entry>

        public operator fun get(name: String): Entry? = of(name)
    }

    public interface Writer {
        public fun put(name: String, resource: SafeResource, isDirectory: Boolean = false): Unit = put(name, Entry(name, resource, isDirectory))

        public fun put(name: String, entry: Entry)

        public fun remove(name: String)

//        public data class ProvidedEntry(
//            override val name: String, override val resource: SafeResource, override val isDirectory: Boolean,
////            private val _uri: URI?,
////            private val _bytes: ByteArray?,
////            private val _inputStream: InputStream?
//        ) : Entry() {
////            override val asBytes: ByteArray =
////                _bytes ?: throw UnsupportedOperationException("Not able to provide bytes of this entry")
////            override val asInputStream: InputStream =
////                _inputStream ?: throw UnsupportedOperationException("Not able to provide an InputStream of this entry")
////            override val asUri: URI =
////                _uri ?: throw UnsupportedOperationException("Not able to provide a URI pointing to this entry")
//        }
    }

    public data class Entry internal constructor(
        public val name: String,
        public val resource: SafeResource,
        public val isDirectory: Boolean,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Entry) return false

            if (name != other.name) return false

            return true
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }
}

//public typealias ArchiveCollisionDetector = (original: ArchiveReference.Entry, toWrite: ArchiveReference.Entry) -> Boolean // True if should overwrite
//
//public fun ArchiveReference.and(
//    other: ArchiveReference,
//    collisionDetector: ArchiveCollisionDetector = { _, _ -> true }
//): ArchiveReference = also {
//    other.reader.entries().forEach {
//        this.reader[it.name]?.takeUnless { o -> collisionDetector(o, it) } ?: writer.put(it)
//    }
//}
