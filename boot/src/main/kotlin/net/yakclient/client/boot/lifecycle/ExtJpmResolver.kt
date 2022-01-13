package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader
import java.lang.module.*

internal class ExtJpmResolver : ExtensionLoader.Resolver<ExtJpmReference> {
    override val accepts: Class<ExtJpmReference> = ExtJpmReference::class.java

    override fun resolve(ref: ExtJpmReference, parent: Extension): ClassLoader {
        val exts: List<String> = ref.finder.findAll()
            .map(ModuleReference::descriptor)
            .map(ModuleDescriptor::name)

        val config: Configuration =
            Configuration.resolve(ref.finder, listOf(parent.module.layer.configuration()), ModuleFinder.of(), exts)


        val controller = ModuleLayer.defineModulesWithOneLoader(config, listOf(parent.module.layer), parent.loader)
        val layer = controller.layer()

        layer.modules().forEach { m ->
            m.packages.forEach { p ->
                controller.addExports(m, p, YakClient::class.java.module)
            }
        }

        return layer.modules()
            .first().classLoader // All the classloaders should be the same, so we just take the first(not pretty but its the only solution as far as i can tell)
    }
}