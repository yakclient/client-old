package net.yakclient.client.boot

import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.setting.ExtensionSettings
import java.io.File
import java.net.URI
import java.nio.file.Path

public data class BootSettings(
    val mcVersion: String,
    val apiVersion: String,
    val mcExtLocation: Path,
    val mcLocation: Path,
    val apiLocation: Path,
    val apiInternalLocation: Path,
    val extensionDir: File,
    val minecraftDependencies : List<URI>,

    val cacheDependencies: Boolean,
    val dependencyCacheLocation: File,
) : ExtensionSettings {
    override val extensionClass: String = YakClient::class.java.name
    override val loader: String? = null
    override val name: String = "YakClient Boot"
    override val dependencies: List<String> = listOf()
    override val repositories: List<RepositorySettings> = listOf()
}
