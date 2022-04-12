package net.yakclient.client.minecraft

import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.util.*
import java.util.*
import java.util.logging.Level

public class MinecraftExtension : Extension() {
    override fun onLoad() {


        logger.log(Level.INFO, "Starting minecraft")
        val startClass = this::class.java.classLoader.loadClass("net.minecraft.client.main.Main")
        startClass.getMethod("main", Array<String>::class.java).invoke(null, arrayOf("--accessToken", "", "--version","1.18.1"))
    }
}