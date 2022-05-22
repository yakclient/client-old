package net.yakclient.client.api.restricted

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.extension.ExtensionLoader

public class ApiExtension : Extension() {
    override fun onLoad() {
        ExtensionLoader.load(YakClient.settings.apiInternalLocation, this).onLoad()
    }
}