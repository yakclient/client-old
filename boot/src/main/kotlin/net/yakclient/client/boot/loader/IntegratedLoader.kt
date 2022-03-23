package net.yakclient.client.boot.loader

public abstract class IntegratedLoader(
    parent: ClassLoader,
    _components: List<ClComponent>
) : ClassLoader(parent) {
    private val packageMap: Map<String, ClComponent> =
        _components.flatMap { c -> c.packages.map { it to c } }.associate { it }

    override fun findClass(name: String): Class<*> =
        findInternal(name) ?: throw ClassNotFoundException(name)

    override fun findClass(moduleName: String?, name: String): Class<*>? = findInternal(name)

    private fun findInternal(name: String): Class<*>? = packageMap[name.packageFormat]?.loadClass(name)
}