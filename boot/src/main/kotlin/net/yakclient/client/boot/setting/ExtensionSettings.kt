package net.yakclient.client.boot.setting

import net.yakclient.client.boot.repository.ArtifactID
import net.yakclient.client.boot.repository.RepositorySettings

public interface ExtensionSettings {
    public val extensionClass: String
    public val name: String
    public val loader: String?
    public val repositories: List<RepositorySettings>?
    public val dependencies: List<ArtifactID>?
//    public val dependencies: List<URI>
//    public val loadChildren: List<URI>
}