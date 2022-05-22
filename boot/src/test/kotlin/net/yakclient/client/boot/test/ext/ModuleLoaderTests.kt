package net.yakclient.client.boot.test.ext

import net.yakclient.client.boot.loader.IntegratedLoader
import net.yakclient.client.util.child
import net.yakclient.client.util.parent
import net.yakclient.client.util.workingDir
import java.lang.module.Configuration
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference
import java.nio.file.Path
import kotlin.test.Test


class ModuleLoaderTests {
    @Test
    fun loadModule() {
        println("Changeed")
        val pluginsDir = Path.of(
            workingDir().parent("client").child("api", "build", "libs").toURI()
        )

        val pluginsFinder = ModuleFinder.of(pluginsDir)

        val plugins = pluginsFinder.findAll().map(ModuleReference::descriptor).map(ModuleDescriptor::name).toList()

        val pluginsConfiguration: Configuration = ModuleLayer
            .boot()
            .configuration()
            .resolve(pluginsFinder, ModuleFinder.of(), plugins)

        val layer = ModuleLayer.boot().defineModules(pluginsConfiguration) {
            ClassLoader.getSystemClassLoader()
        }

        println(layer.modules().first())
    }

    @Test
    fun `Test load module with extension loader`() {
        println("hye")
        IntegratedLoader::class.java.classLoader.getResource(IntegratedLoader::class.java.name.replace('.', '/') + ".class")
    }

}