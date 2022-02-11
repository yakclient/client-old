package net.yakclient.client.api.internal

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.dep.DependencyGraph
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader

public class ApiInternalExt : Extension() {
    override fun onLoad() {
       val ext =  ArchiveUtils.find(YakClient.settings.mcExtLocation)
        val reference =ArchiveUtils.find(YakClient.settings.mcLocation)
//            YakClient.settings.minecraftDependencies.fold(ArchiveUtils.find(YakClient.settings.mcLocation)) { acc, it ->
//                val d = ArchiveUtils.find(it)
//                d.reader.entries().forEach(acc.writer::put)
//                acc
//            }
        val minecraft = ArchiveUtils.resolve(reference, listOf()) //YakClient.settings.minecraftDependencies.map { ArchiveUtils.resolve(ArchiveUtils.find(it), listOf()) }
        val settings = ExtensionLoader.loadSettings(ext)
        ExtensionLoader.load(ext, this, settings = settings, dependencies = ExtensionLoader.loadDependencies(settings).let { it.toMutableList().also { m -> m.add(minecraft) } }).onLoad()
//    println("Should normally load minecraft")

////                ArrayList<URI>().apply {
//                add(YakClient.settings.mcExtLocation)
////                add(YakClient.settings.mcLocation)
////                addAll(YakClient.settings.minecraftDependencies)
//            }
//        ), this).onLoad()
    }
}