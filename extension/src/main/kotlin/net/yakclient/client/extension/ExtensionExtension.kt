package net.yakclient.client.extension

import net.yakclient.client.boot.extension.Extension
import net.yakclient.common.util.immutableLateInit

public class ExtensionExtension : Extension() {
    public var manager: MinecraftExtensionManager by immutableLateInit()

    override fun onLoad() {
        manager = MinecraftExtensionManager(this)
    }
}