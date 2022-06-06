package net.yakclient.client.boot.loader

import net.yakclient.archives.ArchiveHandle
import net.yakclient.common.util.readInputStream
import java.net.URL
import java.nio.ByteBuffer

public class ArchiveSourceProvider(
    private val archive: ArchiveHandle
) : SourceProvider {
    override val packages: Set<String> = archive.reader.entries()
        .map(ArchiveHandle.Entry::name)
        .filter { it.endsWith(".class") }
        .filterNot { it == "module-info.class" }
        .mapTo(HashSet()) { it.removeSuffix(".class").replace('/', '.').packageFormat }

    override fun getClass(name: String): ByteBuffer? =
        archive.reader[name.dotClassFormat]?.resource?.open()?.readInputStream()?.let(ByteBuffer::wrap)

    override fun getResource(name: String): URL? = archive.reader[name]?.resource?.uri?.toURL()
}