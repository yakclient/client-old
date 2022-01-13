package net.yakclient.client.boot

import net.yakclient.client.boot.repository.ArtifactID
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.setting.ExtensionSettings
import java.io.File
import java.net.URI

public data class BootSettings(
    val mcVersion: String,
    val apiVersion: String,
    val mcExtLocation: URI,
    val mcLocation: URI,
    val apiLocation: URI,
    val apiInternalLocation: URI,
    val extensionDir: File,
    val minecraftDependencies : List<URI>
) : ExtensionSettings {
    override val extensionClass: String = YakClient::class.java.name
    override val loader: String? = null
    override val name: String = "YakClient Boot"
    override val dependencies: List<ArtifactID> = listOf()
    override val repositories: List<RepositorySettings> = listOf()
}
