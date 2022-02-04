package net.yakclient.client.boot.internal

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.dep.DependencyReference
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader
import net.yakclient.client.boot.loader.IntegratedCL
import java.lang.module.Configuration
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference
import java.util.*

internal class ExtJpmResolver : ExtensionLoader.Resolver<JpmReference> {
    override val accepts: Class<JpmReference> = JpmReference::class.java

    override fun resolve(ref: JpmReference, parent: Extension, dependencies: List<DependencyReference>): ClassLoader {
        check(dependencies.all { it is JpmDependencyReference }) { "Only JPM dependencies are supported!" }
        val jpmDeps = dependencies as List<JpmDependencyReference>

        val module = parent::class.java.module
        val config: Configuration = Configuration.resolve(
            ProvidedModuleFinder(ref),
            jpmDeps.map(JpmDependencyReference::configuration) + module.layer.configuration(),
            ModuleFinder.of(),
            listOf(ref.descriptor().name())
        )

        val controller = ModuleLayer.defineModulesWithOneLoader(
            config,
            jpmDeps.map(JpmDependencyReference::layer) + module.layer,
            parent.loader
        )

        val layer = controller.layer()
        layer.modules().forEach { m ->
            m.packages.forEach { p ->
                controller.addExports(m, p, YakClient::class.java.module)
            }
        }

        return layer.modules().first().classLoader

//        IntegratedCL.ClController(
//            parent.loader,
//            mutableListOf(LoaderComponent(layer.modules().first().classLoader))  // Only defining one module
//        )
    }

    private class ProvidedModuleFinder(
        private val ref: JpmReference
    ) : ModuleFinder {
        override fun find(name: String): Optional<ModuleReference> =
            if (ref.descriptor().name() == name) Optional.of(ref) else Optional.empty()

        override fun findAll(): MutableSet<ModuleReference> = mutableSetOf(ref)
    }
}