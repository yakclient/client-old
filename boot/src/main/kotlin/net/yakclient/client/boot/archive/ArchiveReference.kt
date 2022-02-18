package net.yakclient.client.boot.archive

import java.io.InputStream
import java.net.URI

public interface ArchiveReference {
    public val name: String
    public val location: URI
    public val reader: Reader
    public val writer: Writer
    public val modified: Boolean

    public interface Reader {
        public fun of(name: String): Entry?

        public fun contains(name: String): Boolean = get(name) == null

        public fun entries(): Set<Entry>

        public operator fun get(name: String): Entry? = of(name)
    }

    public interface Writer {
        public fun put(name: String, b: ByteArray): Unit = put(name, ProvidedEntry(name, null, b, null))

        public fun put(name: String, ins: InputStream): Unit = put(name, ProvidedEntry(name, null, null, ins))

        public fun put(entry: Entry): Unit = put(entry.name, entry)

        public fun put(name: String, entry: Entry)

        private data class ProvidedEntry(
            override val name: String,
            private val _uri: URI?,
            private val _bytes: ByteArray?,
            private val _inputStream: InputStream?
        ) : Entry() {
            override val asBytes: ByteArray =
                _bytes ?: throw UnsupportedOperationException("Not able to provide bytes of this entry")
            override val asInputStream: InputStream =
                _inputStream ?: throw UnsupportedOperationException("Not able to provide an InputStream of this entry")
            override val asUri: URI =
                _uri ?: throw UnsupportedOperationException("Not able to provide a URI pointing to this entry")
        }
    }

    public abstract class Entry {
        public abstract val name: String
        public abstract val asUri: URI
        public abstract val asBytes: ByteArray
        public abstract val asInputStream: InputStream

        final override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Entry) return false

            if (name != other.name) return false

            return true
        }

        final override fun hashCode(): Int {
            return name.hashCode()
        }
    }
}

public typealias ArchiveCollisionDetector = (original: ArchiveReference.Entry, toWrite: ArchiveReference.Entry) -> Boolean // True if should overwrite

public fun ArchiveReference.and(
    other: ArchiveReference,
    collisionDetector: ArchiveCollisionDetector = { _, _ -> true }
): ArchiveReference = also {
    other.reader.entries().forEach {
        this.reader[it.name]?.takeUnless { o -> collisionDetector(o, it) } ?: writer.put(it)
    }
}
