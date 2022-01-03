package net.yakclient.client.boot.ext

import java.io.InputStream
import java.net.URI

public open class ExtensionReference(
    entries: Map<String, ExtensionEntry>
) : Map<String, ExtensionEntry> by entries {
    public fun load(name: String): ExtensionEntry? = this[name]

    public fun exists(name: String): Boolean = containsKey(name)
}

public operator fun ExtensionReference.plus(other: ExtensionReference): ExtensionReference =
    ExtensionReference(this.toMutableMap().apply { putAll(other) })

public interface ExtensionEntry {
    public val name: String

    public fun asURI(): URI

    public fun asInputStream(): InputStream
}