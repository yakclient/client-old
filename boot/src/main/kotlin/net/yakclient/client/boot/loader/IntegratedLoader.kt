package net.yakclient.client.boot.loader

public abstract class IntegratedLoader(
    parent: ClassLoader,
    private val _components: List<ClComponent>
) : ClassLoader(parent) {
    private val componentPackages: Map<String, ClComponent> =
        _components.flatMap { c -> c.packages.map { it to c } }.associate { it }

    override fun findClass(name: String): Class<*>? =
        findInternal(name)

    override fun findClass(moduleName: String?, name: String): Class<*>? = findInternal(name)

    private fun findInternal(name: String): Class<*>? = componentPackages[name.packageFormat]?.loadClass(name)
}