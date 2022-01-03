package net.yakclient.client.boot

import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.lifecycle.BasicExtensionSettings

public class BootModule(
    loader: ClassLoader
) : Extension() {
    init {
        init(loader, BasicExtensionSettings("", "boot"))
    }
}