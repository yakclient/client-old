package net.yakclient.client.boot.setting

import net.yakclient.client.boot.ext.LinkedExtension

public data class LinkedExtensionSettings<T: ExtensionSettings>(override val classloader: ClassLoader, public val settings: T) : LinkedExtension