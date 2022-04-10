package net.yakclient.client.boot.internal

import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.util.readInputStream
import java.io.InputStream
import java.net.URI
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

public class ZipReference(
    private val zip: JarFile, override val location: URI,
) : ArchiveReference {
    private val overrides: MutableMap<String, ArchiveReference.Entry> = HashMap()
    private val removes: MutableSet<String> = HashSet()

    override val reader: ArchiveReference.Reader = ZipReader()
    override val writer: ArchiveReference.Writer = ZipWriter()
    override val modified: Boolean = overrides.isNotEmpty() || removes.isNotEmpty()

    private var closed = false

    override fun close() {
        closed = true
        zip.close()
    }

    private fun ensureOpen() {
        if (closed) {
            throw IllegalStateException("ZipReference is closed")
        }
    }

    private inner class ZipReader : ArchiveReference.Reader {
        override fun of(name: String): ArchiveReference.Entry? {
            ensureOpen()

            val entry: ZipEntry? = zip.getEntry(name)

            return (overrides[name] ?: if (entry != null) object : ArchiveReference.Entry() {
                override val name: String by entry::name
                override val asUri: URI = URI.create("jar:${location}!/$name")
                override val asBytes: ByteArray
                    get() {
                        ensureOpen()
                        return zip.getInputStream(entry).readInputStream()
                    }
                override val asInputStream: InputStream
                    get() {
                        ensureOpen()
                        return zip.getInputStream(entry)
                    }
            } else null)?.takeUnless { removes.contains(name) }
        }

        override fun entries(): Sequence<ArchiveReference.Entry> = zip.entries().asSequence().mapNotNull {
            of(it.name)
        }.onEach {
            ensureOpen()
        }
    }

    private inner class ZipWriter : ArchiveReference.Writer {
        override fun put(name: String, entry: ArchiveReference.Entry) {
            ensureOpen()
            overrides[name] = entry
        }

        override fun remove(name: String) {
            ensureOpen()
            removes.add(name)
        }
    }
}