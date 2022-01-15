package net.yakclient.client.boot.test.ext

import com.typesafe.config.Config
import io.github.config4k.ClassContainer
import io.github.config4k.registerCustomType
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader
import net.yakclient.client.boot.lifecycle.BasicExtensionSettings
import net.yakclient.client.boot.repository.ArtifactID
import net.yakclient.client.util.*
import org.junit.jupiter.api.Test
import java.lang.module.Configuration
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference
import java.nio.file.Path


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
        registerCustomType(UriCustomType())
        registerCustomType(object : TypedMatchingType<ArtifactID>(ArtifactID::class.java), ReadOnlyType {
            override fun parse(clazz: ClassContainer, config: Config, name: String): Any =
                config.getString(name).split(':').let { ArtifactID(it[0], it[1], it[2]) }
        })

        val ext = ExtensionLoader.load(
            ExtensionLoader.reference(workingDir().parent("client").child("api", "build", "libs", "api-1.0-SNAPSHOT.jar").toURI()),
            object : Extension() {
                init {
                    init(
                        ClassLoader.getSystemClassLoader(), BasicExtensionSettings("", "", null, null, null)
                    )
                }
            }
        )
        println(ext)
    }

}