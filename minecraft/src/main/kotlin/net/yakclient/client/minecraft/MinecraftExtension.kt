package net.yakclient.client.minecraft

import net.yakclient.client.boot.ext.Extension
import java.util.logging.Level

public class MinecraftExtension : Extension() {
    override fun onLoad() {
        logger.log(Level.INFO, "Starting minecraft")
        loader.loadClass("net.minecraft.Start").getMethod("main", Array<String>::class.java).invoke(null, emptyArray<String>())
    }
}