package net.yakclient.client.boot.internal

import net.yakclient.client.boot.loader.ClComponent

internal class LoaderComponent(
    private val loader: ClassLoader
) : ClComponent {
    override fun find(name: String): Class<*>? = loader.runCatching { loader.loadClass(name) }.getOrNull()
}