package net.yakclient.client.minecraft

import net.yakclient.client.boot.ext.Extension
import java.util.logging.Level

public class MinecraftExtension : Extension() {
    override fun onLoad() {
        val module = this::class.java.module
        val minecraft =
            module.layer.parents().first { it.modules().any { m -> m.name == "yak.minecraft" } }.modules().first()
        module.addReads(minecraft)

        logger.log(Level.INFO, "Starting minecraft")
        minecraft.classLoader.loadClass("net.minecraft.Start").getMethod("main", Array<String>::class.java).invoke(null, emptyArray<String>())
    }
}