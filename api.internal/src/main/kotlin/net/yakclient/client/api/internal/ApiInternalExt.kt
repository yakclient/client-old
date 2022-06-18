package net.yakclient.client.api.internal

import net.yakclient.archives.ArchiveHandle
import net.yakclient.archives.Archives
import net.yakclient.archives.ResolvedArchive
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.container.ContainerLoader
import net.yakclient.client.boot.container.security.PrivilegeManager
import net.yakclient.client.boot.container.volume.VolumeStore
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.extension.ExtensionInfo
import net.yakclient.client.boot.extension.ExtensionLoader

public class ApiInternalExt : Extension() {
    override fun onLoad() {
        val minecraft: ResolvedArchive = loadMinecraft()

        // Finds the minecraft extension(yakclient) handle
        val ext: ArchiveHandle = Archives.find(YakClient.settings.mcExtLocation, Archives.Finders.JPM_FINDER)

        // Loads settings
        val settings = ExtensionLoader.loadSettings(ext)

        // Loads the extension
        ContainerLoader.load(
            ExtensionInfo(
                ext,
                this,
                settings,
                ExtensionLoader.loadDependencies(settings).let {
                    it.toMutableSet().also { m -> m.add(minecraft) }
                }
            ),
            ExtensionLoader,
            VolumeStore["minecraft-data"],
            PrivilegeManager.allPrivileges(),
            loader
        ).process.start()
    }

}