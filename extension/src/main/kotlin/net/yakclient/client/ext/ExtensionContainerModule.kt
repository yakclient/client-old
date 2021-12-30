package net.yakclient.client.ext

import net.yakclient.client.ext.loader.ExtensionModule
import net.yakclient.client.internal.extension.ExtensionLoader
import net.yakclient.client.internal.extension.ExtensionReference
import net.yakclient.client.internal.extension.Extension

public sealed class ExtensionContainerModule(
    override val parent: Extension,
    override val loader: ClassLoader
) : Extension {
    protected abstract val extensionLoader: ExtensionLoader

    public fun loadExtension(ref: ExtensionReference): ExtensionModule =
        extensionLoader.load(ref) as? ExtensionModule
            ?: throw IllegalStateException("Invalid ModuleLoader, must load a extension module")
}