package net.yakclient.client.api.internal

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader
import net.yakclient.client.boot.archive.ArchiveUtils

public class ApiExtension : Extension() {
    override fun onLoad() {
         try {
             ExtensionLoader.load(ArchiveUtils.find(YakClient.settings.apiInternalLocation), this).onLoad()
        } catch (e: Exception) {
            YakClient.exit(e, "Failed to load the Yak Internal API!")
        }
    }
}