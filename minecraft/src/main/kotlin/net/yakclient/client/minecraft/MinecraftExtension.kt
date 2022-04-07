package net.yakclient.client.minecraft

import net.yakclient.client.boot.extension.Extension
import java.util.logging.Level

public class MinecraftExtension : Extension() {
    override fun onLoad() {
        val module = this::class.java.module
        val minecraft =
            module.layer.parents().first { it.modules().any { m -> m.name == "minecraft" } }.modules().first()
        module.addReads(minecraft)

        logger.log(Level.INFO, "Starting minecraft")
        val startClass = minecraft.classLoader.loadClass("net.minecraft.client.main.Main")
        startClass.getMethod("main", Array<String>::class.java).invoke(null, emptyArray<String>())
    }
}