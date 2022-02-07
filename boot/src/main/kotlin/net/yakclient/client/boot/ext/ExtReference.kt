package net.yakclient.client.boot.ext

import java.io.InputStream
import java.net.URI

public interface ExtReference {
    public val name: String
    public val location: URI
    public val reader : Reader
    public val writer : Writer

    public interface Reader {
        public fun of(name: String) : Entry?

        public fun contains(name: String) : Boolean = get(name) == null

        public fun listEntries() : List<Entry>

        public operator fun get(name: String) : Entry? = of(name)
    }

    public interface Writer {
        public fun put(name: String, b: ByteArray): Unit = put(name, ProvidedEntry(name, null, b, null))

        public fun put(name: String, ins: InputStream) : Unit = put(name, ProvidedEntry(name, null, null, ins))

        public fun put(entry: Entry): Unit = put(entry.name, entry)

        public fun put(name: String, entry: Entry)

        private data class ProvidedEntry(
            override val name: String,
            private val  _uri: URI?,
            private val  _bytes: ByteArray?,
            private val _inputStream: InputStream?
        ) : Entry {
            override val asBytes: ByteArray = _bytes ?: throw UnsupportedOperationException("Not able to provide bytes of this entry")
            override val asInputStream: InputStream = _inputStream ?: throw UnsupportedOperationException("Not able to provide an InputStream of this entry")
            override val asUri: URI = _uri ?: throw UnsupportedOperationException("Not able to provide a URI pointing to this entry")

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ProvidedEntry

                if (_uri != other._uri) return false
                if (_bytes != null) {
                    if (other._bytes == null) return false
                    if (!_bytes.contentEquals(other._bytes)) return false
                } else if (other._bytes != null) return false
                if (_inputStream != other._inputStream) return false

                return true
            }

            override fun hashCode(): Int {
                var result = _uri?.hashCode() ?: 0
                result = 31 * result + (_bytes?.contentHashCode() ?: 0)
                result = 31 * result + (_inputStream?.hashCode() ?: 0)
                return result
            }
        }
    }

    public interface Entry {
        public val name: String
        public val asUri: URI
        public val asBytes: ByteArray
        public val asInputStream: InputStream
    }
}