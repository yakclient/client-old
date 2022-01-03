package net.yakclient.client.api

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.YakExtensionManager
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.lifecycle.loadJar

public class ApiExtension : Extension() {
    override fun onLoad() {
         try {
            val moduleLoader = YakExtensionManager.extLoader(this)
            moduleLoader.loadJar(YakClient.settings.apiInternalLocation)
        } catch (e: Exception) {
            YakClient.exit(e, "Failed to load the Yak Internal API!")
        }
    }
}