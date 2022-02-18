package net.yakclient.client.boot.loader

public abstract class IntegratedLoader(
    parent: ClassLoader,
    _components: List<ClComponent>
) : ClassLoader(parent) {
    private val packageMap : Map<String, ClComponent> = _components.flatMap { c -> c.packages.map { it to c } }.associate { it }

    private fun String.packageOf() : String = substring(0, lastIndexOf('.'))

    override fun findClass(name: String): Class<*> = packageMap[name.packageOf()]?.loadClass(name) ?: throw ClassNotFoundException(name)
}