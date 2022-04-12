package net.yakclient.client.boot.internal.zip

import net.yakclient.client.boot.archive.ArchiveHandle
import net.yakclient.client.util.readInputStream
import net.yakclient.client.util.resource.LocalResource
import java.io.InputStream
import java.net.URI
import java.util.jar.JarFile
import java.util.zip.ZipEntry

public class ZipHandle(
    private val zip: JarFile, override val location: URI,
) : ArchiveHandle {
    private var closed = false
    private val overrides: MutableMap<String, ArchiveHandle.Entry> = HashMap()
    private val removes: MutableSet<String> = HashSet()

    override val reader: ArchiveHandle.Reader = ZipReader()
    override val writer: ArchiveHandle.Writer = ZipWriter()
    override val modified: Boolean = overrides.isNotEmpty() || removes.isNotEmpty()
    override val isClosed: Boolean
        get() = closed

    override fun close() {
        closed = true
        zip.close()
    }

    private fun ensureOpen() {
        if (closed) {
            throw IllegalStateException("ZipReference is closed")
        }
    }

    private inner class ZipReader : ArchiveHandle.Reader {
        override fun of(name: String): ArchiveHandle.Entry? {
            ensureOpen()

            val entry = zip.getJarEntry(name) ?: return null

            val uri = URI.create("jar:${location}!/${entry.realName}")

            return overrides[name] ?: ArchiveHandle.Entry(
                entry.name,
                LocalResource(uri),
                entry.isDirectory
            ).takeUnless { removes.contains(name) }
        }

        override fun entries(): Sequence<ArchiveHandle.Entry> = zip.entries().asSequence().mapNotNull {
            of(it.name)
        }.onEach {
            ensureOpen()
        }
    }

    private inner class ZipWriter : ArchiveHandle.Writer {
        override fun put(name: String, entry: ArchiveHandle.Entry) {
            ensureOpen()
            overrides[name] = entry
        }

        override fun remove(name: String) {
            ensureOpen()
            removes.add(name)
        }
    }
}