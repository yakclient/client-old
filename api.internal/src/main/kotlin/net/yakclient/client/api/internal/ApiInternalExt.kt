package net.yakclient.client.api.internal

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader
import net.yakclient.client.util.children

public class ApiInternalExt : Extension() {
    override fun onLoad() {
//        ExtensionLoader.load(
//       val ext =  ExtensionLoader.find(YakClient.settings.mcExtLocation)
//       val minecraft = YakClient.settings.mcLocation.children().fold(ExtensionLoader.find(YakClient.settings.mcLocation)) { acc, it ->
//            val d = ExtensionLoader.find(it)
//            d.reader.listEntries().forEach(acc.writer::put)
//            acc
//        }
//        val settings = ExtensionLoader.loadSettings(ext)
//        ExtensionLoader.load(ext, this, settings = settings, dependencies = ExtensionLoader.loadDependencies(settings).let { it.toMutableList().also { it.add(minecraft) } })
    println("Should normally load minecraft")

////                ArrayList<URI>().apply {
//                add(YakClient.settings.mcExtLocation)
////                add(YakClient.settings.mcLocation)
////                addAll(YakClient.settings.minecraftDependencies)
//            }
//        ), this).onLoad()
    }
}