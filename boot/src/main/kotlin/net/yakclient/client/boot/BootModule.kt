package net.yakclient.client.boot

import net.yakclient.client.internal.extension.Extension

public class BootModule(override val loader: ClassLoader) : Extension {
    override val parent: Extension? = null


}