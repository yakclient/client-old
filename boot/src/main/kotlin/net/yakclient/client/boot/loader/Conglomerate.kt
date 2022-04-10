package net.yakclient.client.boot.loader

import net.yakclient.client.boot.archive.ArchiveReference
import java.net.URI
import java.net.URL
import java.nio.ByteBuffer
import java.security.CodeSource
import java.security.ProtectionDomain
import java.security.cert.Certificate
import java.util.*
import kotlin.collections.HashSet

public class ClConglomerate(
    parent: ClassLoader,
    private val providers: List<ConglomerateProvider>
) : ClassLoader(parent), ClComponent {
    override val packages: Set<String> = providers.flatMapTo(HashSet(), ConglomerateProvider::packages)

    private fun <V, K> Iterable<V>.flatAssociateBy(transformer: (V) -> Iterable<K>): Map<K, V> =
        flatMap { v -> transformer(v).map { it to v } }.associate { it }

    private val packageMap: Map<String, ConglomerateProvider> = providers.flatAssociateBy { it.packages }
//    private val resourceMap: Map<String, ConglomerateProvider> = _providers.flatAssociateBy { it.resources }

    private val domain = ProtectionDomain(CodeSource(null, arrayOf<Certificate>()), null, this, null)

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        findLoadedClass(name)?.let { return it }

        val bb: ByteBuffer = packageMap[name.packageFormat]?.provideClass(name) ?: return super.loadClass(name, resolve)
        return defineClass(name, bb, domain).also { if (resolve) resolveClass(it) }
    }

    override fun findResources(name: String): Enumeration<URL> {
        return Collections.enumeration(providers.mapNotNull { it.provideResource(name)?.toURL() })
    }

    override fun findResource(name: String): URL? =
        providers.firstNotNullOfOrNull { it.provideResource(name) }?.toURL()

    override fun findResource(mn: String, name: String): URL? = findResource(name)
}

public interface ConglomerateProvider {
    public val packages: Set<String>
    public val resources: Set<String>

    public fun provideClass(name: String): ByteBuffer?

    public fun provideResource(name: String): URI?
}

public open class ArchiveConglomerateProvider(
    private val archive: ArchiveReference
) : ConglomerateProvider {
    override val packages: Set<String> = archive.reader.entries()
        .map(ArchiveReference.Entry::name)
        .filter { it.endsWith(".class") }
        .filterNot { it == "module-info.class" }
        .mapTo(HashSet()) { it.removeSuffix(".class").replace('/', '.').packageFormat }
    override val resources: Set<String> = archive.reader.entries()
        .map { it.name }
        .filterNotTo(HashSet()) { it.endsWith(".class") }

    override fun provideClass(name: String): ByteBuffer? =
        archive.reader[name.dotClassFormat]?.asBytes?.let(ByteBuffer::wrap)// ByteBuffer.wrap()

    override fun provideResource(name: String): URI? = archive.reader[name]?.asUri
}