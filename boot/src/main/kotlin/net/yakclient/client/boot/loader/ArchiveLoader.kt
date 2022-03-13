package net.yakclient.client.boot.loader

import net.yakclient.client.boot.archive.ArchiveReference
import java.nio.ByteBuffer
import java.security.CodeSource
import java.security.ProtectionDomain
import java.security.cert.Certificate

public open class ArchiveLoader(
    parent: ClassLoader,
    _components: List<ClComponent>,
    private val reference: ArchiveReference
) : IntegratedLoader(
    parent, _components
) {
    private val domain = ProtectionDomain(CodeSource(null, arrayOf<Certificate>()), null, this, null)

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        findLoadedClass(name)?.let { return it }
        val entry: ArchiveReference.Entry =
            reference.reader["${name.replace('.', '/')}.class"] ?: return super.loadClass(name, resolve)

        val bb = ByteBuffer.wrap(entry.asBytes)

        return defineClass(name, bb, domain).also { if (resolve) resolveClass(it) }
    }
}