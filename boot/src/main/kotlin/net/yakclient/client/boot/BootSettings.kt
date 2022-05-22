package net.yakclient.client.boot

import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.extension.ExtensionSettings
import java.io.File
import java.nio.file.Path
import net.yakclient.common.util.resolve

public data class BootSettings(
    val mcVersion: String,
    val apiVersion: String,
    val mcExtLocation: Path,
    val clientJsonFile: Path,
    val apiLocation: Path,
    val apiInternalLocation: Path,
    val extensionDir: File,
//    val minecraftDependencies : List<Path>,

    val cacheDependencies: Boolean,
    val dependencyCacheLocation: File,

    val tempDir: Path,
    val moduleTemp: Path,
    val minecraftDir: Path,
    val minecraftLibDir: Path,
    val minecraftNativesDir: Path,
) : ExtensionSettings {
    override val extensionClass: String = YakClient::class.java.name
    override val loader: String? = null
    override val name: String = "YakClient Boot"
    override val dependencies: List<String> = listOf()
    override val repositories: List<RepositorySettings> = listOf()

    public val tempPath: Path = YakClient.yakDir resolve tempDir
    public val moduleTempPath: Path = tempPath resolve moduleTemp
    public val minecraftPath: Path = YakClient.yakDir resolve minecraftDir
}
