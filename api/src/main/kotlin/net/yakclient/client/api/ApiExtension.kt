package net.yakclient.client.api

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader

public class ApiExtension : Extension() {
    override fun onLoad() {
         try {
             ExtensionLoader.load(ExtensionLoader.find(YakClient.settings.apiInternalLocation), this).onLoad()
        } catch (e: Exception) {
            YakClient.exit(e, "Failed to load the Yak Internal API!")
        }
    }
}