package net.yakclient.client.api.internal

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader
import java.net.URI

public class ApiInternalExt : Extension() {
    override fun onLoad() {
//        ExtensionLoader.load(
//            ExtensionLoader.find(ArrayList<URI>().apply {
//                add(YakClient.settings.mcExtLocation)
//                add(YakClient.settings.mcLocation)
//                addAll(YakClient.settings.minecraftDependencies)
//            }
//        ), this).onLoad()
    }
}