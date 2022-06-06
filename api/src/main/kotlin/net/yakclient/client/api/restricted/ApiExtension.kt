package net.yakclient.client.api.restricted

import net.yakclient.archives.Archives
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.container.ContainerLoader
import net.yakclient.client.boot.container.VolumeStore
import net.yakclient.client.boot.container.security.allPrivileges
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.extension.ExtensionInfo
import net.yakclient.client.boot.extension.ExtensionLoader

public class ApiExtension : Extension() {
    override fun onLoad() {
        ContainerLoader.load(
            ExtensionInfo(
                Archives.find(YakClient.settings.apiInternalLocation, Archives.Finders.JPM_FINDER),
                this,
            ),
            ExtensionLoader,
            VolumeStore["api-data"],
            allPrivileges(),
            loader
        ).process.start()
//        ExtensionLoader.load(YakClient.settings.apiInternalLocation, this).onLoad()
    }
}