package net.yakclient.client.extension

import io.github.config4k.extract
import io.github.config4k.toConfig
import net.yakclient.archives.Archives
import net.yakclient.client.boot.container.Container
import net.yakclient.client.boot.container.ContainerLoader
import net.yakclient.client.boot.container.security.FileAction
import net.yakclient.client.boot.container.security.FilePrivilege
import net.yakclient.client.boot.container.security.PrivilegeManager
import net.yakclient.client.boot.container.volume.VolumeStore
import net.yakclient.client.boot.container.volume.absoluteRoot
import net.yakclient.client.boot.extension.BasicExtensionSettings
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.extension.ExtensionInfo
import net.yakclient.client.boot.extension.ExtensionLoader
import net.yakclient.client.util.toConfig
import java.nio.file.Path
import java.util.UUID

public object MinecraftExtensionLoader {
    public fun load(path: Path, parent: Extension): Container {
        val archive = Archives.find(path, Archives.Finders.JPM_FINDER)
        val settings = archive.reader["ext-settings.conf"]?.resource?.uri?.toConfig()
            ?.extract<MinecraftExtensionSettings>("loader")
            ?: throw IllegalArgumentException("Extension must have a settings file! Please add a 'ext-settings.conf' to your build!")
        val dependencies = ExtensionLoader.loadDependencies(settings)

        val info = ExtensionInfo(archive, parent, settings, dependencies)

        fun uniqueVolumeName(): String {
            val uuid = UUID.randomUUID().toString()

            return if (!VolumeStore.contains(uuid)) uuid
            else uniqueVolumeName()
        }

        val volume = VolumeStore[settings.volumeName ?: uniqueVolumeName()]

        return ContainerLoader.load(
            info,
            ExtensionLoader,
            volume,
            PrivilegeManager.createPrivileges(
                FilePrivilege(
                    volume.absoluteRoot().resolve("-").toString(),
                    FileAction.ALL
                )
            ),
            parent.loader
        )
    }
}