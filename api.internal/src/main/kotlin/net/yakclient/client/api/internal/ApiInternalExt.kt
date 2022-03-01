package net.yakclient.client.api.internal

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.archive.ArchiveUtils.resolve
import net.yakclient.client.boot.archive.patch
import net.yakclient.client.boot.archive.removeModules
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader
import net.yakclient.client.boot.loader.ArchiveConglomerateProvider
import net.yakclient.client.boot.loader.ClConglomerate

public class ApiInternalExt : Extension() {
    override fun onLoad() {
        val ext = ArchiveUtils.find(YakClient.settings.mcExtLocation)
        val reference = ArchiveUtils.find(YakClient.settings.mcLocation)

        val mcDeps = YakClient.settings.minecraftDependencies.map(ArchiveUtils::find).patch(
            setOf("codecjorbis", "codecwav"),
            setOf("netty", "netty.all"),
            setOf("librarylwjglopenal", "libraryjavasound")
        )

        val loader = ClConglomerate(loader, (mcDeps + reference).map(::ArchiveConglomerateProvider))

        val minecraft = resolve(
             mcDeps + reference,
        ) { loader }
        val settings = ExtensionLoader.loadSettings(ext)

        ExtensionLoader.load(
            ext,
            this,
            settings = settings,
            dependencies = ExtensionLoader.loadDependencies(settings)
                .let { it.toMutableList().also { m -> m.addAll(minecraft) } }).onLoad()
    }
}