package net.yakclient.client.ext.loader

import net.yakclient.client.api.ext.Extension
import net.yakclient.client.internal.extension.Extension

public data class ExtensionModule(
    val ext: Extension,
    override val parent: net.yakclient.client.internal.extension.Extension?,
    override val loader: ClassLoader,
) : net.yakclient.client.internal.extension.Extension