package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.ext.ExtReference
import net.yakclient.client.util.openStream
import net.yakclient.client.util.readInputStream
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleReader
import java.lang.module.ModuleReference
import java.net.URI
import java.nio.ByteBuffer
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.stream.Stream

internal class JpmReference(
    descriptor: ModuleDescriptor,
    override val location: URI
) : ExtReference, ModuleReference(descriptor, location) {
    private val overrides: MutableMap<String, ExtReference.ExtRefEntry> = HashMap()

    override val reader: ExtReference.Reader = JpmReader(JarFile(location.path))
    override val writer: ExtReference.Writer = JpmWriter()

    override fun open(): ModuleReader = reader as ModuleReader

    private inner class JpmReader(
        private val jar: JarFile
    ) : ExtReference.Reader, ModuleReader, Closeable by jar {
        private val cache: MutableMap<String, ExtReference.ExtRefEntry> = HashMap()

        override fun of(name: String): ExtReference.ExtRefEntry? =
            overrides[name] ?: (cache[name] ?: jar.getJarEntry(name)?.let { e -> JarEntryReference(e).also { cache[name] = it } })

        override fun find(name: String): Optional<URI> = Optional.ofNullable(of(name)?.asUri)

        override fun open(name: String): Optional<InputStream> = Optional.ofNullable(of(name)?.asInputStream)

        override fun read(name: String): Optional<ByteBuffer> = Optional.ofNullable(of(name)?.asBytes?.let { ByteBuffer.wrap(it) } )

        override fun release(bb: ByteBuffer) {
            super.release(bb)
        }

        override fun list(): Stream<String> = Stream.concat(jar.stream().map(JarEntry::getName), cache.keys.stream())
    }

    private inner class JpmWriter : ExtReference.Writer {
        override fun put(name: String, entry: ExtReference.ExtRefEntry): Unit = let { overrides[name] = entry }
    }

    private inner class JarEntryReference(
        entry: JarEntry
    ) : ExtReference.ExtRefEntry {
        override val name: String = entry.name
        override val asUri: URI =
            URI.create("jar:file:${File.separator}${location.path.removePrefix("/")}!/${entry.name}") // TODO test alot
        override val asBytes: ByteArray = readInputStream(asUri.openStream()) //Test if another method could be used.
        override val asInputStream: InputStream = asUri.openStream()
    }
}