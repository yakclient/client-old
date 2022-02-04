package net.yakclient.client.api.internal

import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader

public class ApiInternalExt : Extension() {
    override fun onLoad() {
        ExtensionLoader.load(
            ExtensionLoader.find(
//                ArrayList<URI>().apply {
//                add(YakClient.settings.mcExtLocation)
//                add(YakClient.settings.mcLocation)
//                addAll(YakClient.settings.minecraftDependencies)
            }
        ), this).onLoad()
    }
}