package net.yakclient.client.boot.extension

import net.yakclient.client.boot.repository.RepositorySettings

public data class BasicExtensionSettings(
    override val extensionClass: String,
    override val name: String,
    override val loader: String?,
    override val repositories: List<RepositorySettings>?,
    override val dependencies: List<String>?,
) : ExtensionSettings
