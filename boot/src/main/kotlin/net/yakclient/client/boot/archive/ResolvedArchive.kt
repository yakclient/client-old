package net.yakclient.client.boot.archive

public interface ResolvedArchive {
    public val classloader: ClassLoader
    public val packages: Set<String>

    public fun loadService(name: String) : List<Class<*>>
}