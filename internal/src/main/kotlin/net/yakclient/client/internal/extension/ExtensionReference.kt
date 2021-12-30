package net.yakclient.client.internal.extension

import java.io.InputStream

public open class ExtensionReference(
    entries: Map<String,ExtensionEntry>
) : Map<String, ExtensionEntry> by entries{
    public fun load(name: String): ExtensionEntry? = this[name]

    public fun loadClass(name: String, classLoader: ClassLoader = ClassLoader.getSystemClassLoader()) : Class<*>? = TODO("Implement")

    public fun exists(name: String): Boolean = containsKey(name)
}

public interface ExtensionEntry {
    public val name: String

    public fun asInputStream() : InputStream
}