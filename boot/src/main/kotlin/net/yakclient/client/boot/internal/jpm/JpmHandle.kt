package net.yakclient.client.boot.internal.jpm

import net.yakclient.client.boot.archive.ArchiveHandle
import net.yakclient.client.util.openStream
import net.yakclient.client.util.readInputStream
import java.io.InputStream
import java.lang.module.ModuleReader
import java.lang.module.ModuleReference
import java.net.URI
import java.nio.ByteBuffer
import java.util.*
import java.util.stream.Stream

public class JpmHandle(
    delegate: ModuleReference,
) : ArchiveHandle, ModuleReference(
    delegate.descriptor(),
    delegate.location().orElseGet { null }
) {
    private var closed: Boolean = false
    private val overrides: MutableMap<String, ArchiveHandle.Entry> = HashMap()
    private val removes: MutableSet<String> = HashSet()

    override val location: URI = delegate.location().get()
    override val reader: ArchiveHandle.Reader = JpmReader(delegate.open())
    override val writer: ArchiveHandle.Writer = JpmWriter()
    override val modified: Boolean get() = overrides.isNotEmpty() || removes.isNotEmpty()
    override val isClosed: Boolean
        get() = closed

    override fun close() {
        closed = true
        (reader as JpmReader).close()
    }

    private fun ensureOpen() {
        if (closed) {
            throw IllegalStateException("Module is closed")
        }
    }

    override fun open(): ModuleReader = reader as ModuleReader

    private inner class JpmReader(
        private val reader: ModuleReader
    ) : ArchiveHandle.Reader, ModuleReader by reader {
        private val cache: MutableMap<String, ArchiveHandle.Entry> = HashMap()

        override fun of(name: String): ArchiveHandle.Entry? {
            ensureOpen()

            return (overrides[name]
                ?: cache[name]
                ?: reader.find(name).orElse(null)?.let { JpmEntryRef(name, it) }?.also { cache[name] = it })
                ?.takeUnless { removes.contains(it.name) }
        }

        override fun entries(): Sequence<ArchiveHandle.Entry> = Sequence {
            list().iterator()
        }.mapNotNull { of(it) }

        override fun find(name: String): Optional<URI> = Optional.ofNullable(of(name)?.asUri)

        override fun open(name: String): Optional<InputStream> = Optional.ofNullable(of(name)?.asInputStream)

        override fun read(name: String): Optional<ByteBuffer> =
            Optional.ofNullable(of(name)?.asBytes?.let { ByteBuffer.wrap(it) })

        override fun list(): Stream<String> {
            ensureOpen()
            return Stream.concat(overrides.keys.stream(), reader.list())
        }
    }

    private inner class JpmWriter : ArchiveHandle.Writer {
        override fun put(name: String, entry: ArchiveHandle.Entry) {
            ensureOpen()
            overrides[name] = entry
        }

        override fun remove(name: String) {
            ensureOpen()
            removes.add(name)
        }
    }

    private data class JpmEntryRef(
        override val name: String,
        override val asUri: URI
    ) : ArchiveHandle.Entry() {
        override val asInputStream: InputStream
            get() = asUri.openStream()
        override val asBytes: ByteArray
            get() = asInputStream.readInputStream()
    }
}