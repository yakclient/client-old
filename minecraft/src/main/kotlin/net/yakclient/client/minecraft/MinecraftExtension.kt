package net.yakclient.client.minecraft

import net.yakclient.archives.Archives
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.container.ContainerLoader
import net.yakclient.client.boot.container.security.PrivilegeManager
import net.yakclient.client.boot.container.volume.VolumeStore
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.extension.ExtensionInfo
import net.yakclient.client.boot.extension.ExtensionLoader
import net.yakclient.common.util.*
import java.util.*
import java.util.logging.Level

public class MinecraftExtension : Extension() {
    override fun onLoad() {
        logger.log(Level.INFO, "Starting minecraft")
        val startClass = this::class.java.classLoader.loadClass("net.minecraft.client.main.Main")


        val runningThread = Thread({
            ContainerLoader.load(
                ExtensionInfo(
                    Archives.find(YakClient.settings.extLocation, Archives.Finders.JPM_FINDER),
                    this,
                ),
                ExtensionLoader,
                VolumeStore["api-data"],
                PrivilegeManager.allPrivileges(),
                loader
            ).process.start()
        }, "YakClient Ig, Idk")

        runningThread.start()


        startClass.getMethod("main", Array<String>::class.java)
            .invoke(null, arrayOf("--accessToken", "", "--version", "1.18.2"))
    }
}