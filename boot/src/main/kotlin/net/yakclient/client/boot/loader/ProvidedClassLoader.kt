package net.yakclient.client.boot.loader

import net.yakclient.client.boot.container.ContainerSource
import java.net.URL
import java.nio.ByteBuffer
import java.security.CodeSource
import java.security.Permissions
import java.security.ProtectionDomain
import java.security.cert.Certificate

public open class ProvidedClassLoader(
    private val provider: SourceProvider,
    _components: List<ClComponent>,
    parent: ClassLoader
) : IntegratedLoader(
    _components,
    parent
) {
    private val defaultDomain = ProtectionDomain(CodeSource(null, arrayOf<Certificate>()), Permissions(), this, null)

    override fun findResource(name: String): URL? =
        provider.getResource(name)

    override fun findResource(mn: String, name: String): URL? = findResource(name)

    override fun findClass(name: String): Class<*>? =
        loadLocalClass(name) ?: super.findClass(name)

    override fun findClass(moduleName: String?, name: String): Class<*>? = findClass(name)

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return (loadLocalClass(name) ?: super.loadClass(name, false))?.also { if (resolve) resolveClass(it) }
            ?: throw ClassNotFoundException(name)
    }

    protected open fun loadLocalClass(name: String): Class<*>? {
        return loadLocalClass(name, provider.getClass(name) ?: return null)
    }

    protected open fun loadLocalClass(name: String, buffer: ByteBuffer): Class<*> {
        return defineClass(name, buffer, defaultDomain)
    }
}

// Name in non-internal jvm format(Eg. java.lang.String)
public interface SourceProvider {
    public val packages: Set<String>

    public fun getClass(name: String): ByteBuffer?

    public fun getResource(name: String): URL?
}