package net.yakclient.client.boot.loader

import net.yakclient.client.boot.archive.ArchiveHandle
import net.yakclient.client.util.readInputStream
import java.nio.ByteBuffer
import java.security.CodeSource
import java.security.ProtectionDomain
import java.security.cert.Certificate

// TODO Currently no archive handles are being closed, the best solution i can think of is to have some soft of collection service that keeps track of all loaders and closes them when the JVM exits or if they are not being used anymore
public class ArchiveLoader(
    parent: ClassLoader,
    components: List<ClComponent>,
    private val handle: ArchiveHandle
) : IntegratedLoader(parent, components) {
    private val domain = ProtectionDomain(CodeSource(null, arrayOf<Certificate>()), null, this, null)
    init {
        check(handle.isOpen) { "Given reference: ${handle.location} must be open!" }
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        findLoadedClass(name)?.let { return it }

        val entry: ArchiveHandle.Entry =
            handle.reader["${name.replace('.', '/')}.class"] ?: return super.loadClass(name, resolve)

        val bb = ByteBuffer.wrap(entry.resource.open().readInputStream())

        return defineClass(name, bb, domain).also { if (resolve) resolveClass(it) }
    }
}