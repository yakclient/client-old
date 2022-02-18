package net.yakclient.client.boot.loader

import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.exception.EntryNotFoundException
import java.nio.ByteBuffer
import java.security.CodeSource
import java.security.ProtectionDomain
import java.security.cert.Certificate

public class ClConglomerate(
    parent: ClassLoader,
    _providers: List<ConglomerateProvider>
) : ClassLoader(parent), ClComponent {
    private val providers: Map<String, ConglomerateProvider> =
        _providers.flatMap { p -> p.all.map { it to p } }.associate { it }
    override val all: List<String> = providers.keys.toList()

    private val domain = ProtectionDomain(CodeSource(null, arrayOf<Certificate>()), null, this, null)

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        val bb: ByteBuffer = providers[name]?.provide(name) ?: return super.loadClass(name)

        return defineClass(name, bb, domain).also { if (resolve) resolveClass(it) }
    }
}

public interface ConglomerateProvider {
    public val all: List<String>

    public fun provide(name: String): ByteBuffer
}

public class ArchiveConglomerateProvider(
    private val archive: ArchiveReference
) : ConglomerateProvider {
    override val all: List<String> get() = archive.reader.entries().map { it.name }

    override fun provide(name: String): ByteBuffer =
        ByteBuffer.wrap(archive.reader[name]?.asBytes ?: throw EntryNotFoundException(archive, name))
}