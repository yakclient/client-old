package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.setting.ExtensionSettings

public data class BasicExtensionSettings(
    override val extensionClass: String,
    override val name: String,
) : ExtensionSettings
