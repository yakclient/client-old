package net.yakclient.client.internal.extension

import java.io.InputStream

public class NamedExtensionEntry(
    override val name: String,
    private val provider: () -> InputStream
) : ExtensionEntry {
    override fun asInputStream(): InputStream = provider()
}