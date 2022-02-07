package net.yakclient.client.boot.internal

import net.yakclient.client.boot.ext.ExtReference
import net.yakclient.client.util.openStream
import net.yakclient.client.util.readInputStream
import java.io.InputStream
import java.lang.module.ModuleReader
import java.lang.module.ModuleReference
import java.net.URI
import java.nio.ByteBuffer
import java.util.*
import java.util.stream.Stream

internal class JpmReference(
    private val delegate: ModuleReference,
) : ExtReference, ModuleReference(
    delegate.descriptor(),
    delegate.location()
        .orElseThrow { IllegalArgumentException("JpmReference must have a URI associated with its delegate!") }) {
    private val overrides: MutableMap<String, ExtReference.Entry> = HashMap()

    override val name: String = delegate.descriptor().name()
    override val location: URI = delegate.location().get()
    override val reader: ExtReference.Reader = JpmReader(delegate.open())
    override val writer: ExtReference.Writer = JpmWriter()

    override fun open(): ModuleReader = reader as ModuleReader

    private inner class JpmReader(
        private val reader: ModuleReader
    ) : ExtReference.Reader, ModuleReader by reader {
        private val cache: MutableMap<String, ExtReference.Entry> = HashMap()

        override fun of(name: String): ExtReference.Entry? =
            overrides[name] ?: cache[name] ?: delegate.open().find(name).orElse(null)?.let { JpmEntryRef(name, it) }
                ?.also { cache[name] = it }

        override fun listEntries(): List<ExtReference.Entry> = list().toList().mapNotNull(::of)

        override fun find(name: String): Optional<URI> = Optional.ofNullable(of(name)?.asUri)

        override fun open(name: String): Optional<InputStream> = Optional.ofNullable(of(name)?.asInputStream)

        override fun read(name: String): Optional<ByteBuffer> =
            Optional.ofNullable(of(name)?.asBytes?.let { ByteBuffer.wrap(it) })

        override fun list(): Stream<String> = Stream.concat(reader.list(), overrides.keys.stream())
    }

    private inner class JpmWriter : ExtReference.Writer {
        override fun put(name: String, entry: ExtReference.Entry): Unit = let { overrides[name] = entry }
    }

    private inner class JpmEntryRef(
        override val name: String,
        override val asUri: URI
    ) : ExtReference.Entry {
        override val asInputStream: InputStream
            get() = asUri.openStream()
        override val asBytes: ByteArray
            get() = asInputStream.readInputStream()
    }
}