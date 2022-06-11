package net.yakclient.client.extension

import net.yakclient.client.boot.extension.ExtensionSettings
import net.yakclient.client.boot.repository.RepositorySettings

public data class MinecraftExtensionSettings(
    override val name: String,
    override val extensionClass: String,
    override val repositories: List<RepositorySettings>?,
    override val dependencies: List<String>?,
    public val volumeName: String?

) : ExtensionSettings {
}