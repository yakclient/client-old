package net.yakclient.client.boot.extension

import net.yakclient.client.boot.repository.RepositorySettings

public interface ExtensionSettings {
    public val name: String
    public val extensionClass: String
    public val repositories: List<RepositorySettings>?
    public val dependencies: List<String>?
}