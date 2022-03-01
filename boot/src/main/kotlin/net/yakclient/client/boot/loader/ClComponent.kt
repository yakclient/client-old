package net.yakclient.client.boot.loader

public interface ClComponent{
    public val packages : Set<String>

    public fun loadClass(name: String) : Class<*>
}
