package net.yakclient.client.boot.internal.jpm

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.archive.ResolvedArchive
import java.lang.module.Configuration

internal class ResolvedJpm(
    val module: Module//, override val reference: ArchiveReference
) : ResolvedArchive {
    override val classloader: ClassLoader = module.classLoader ?: YakClient.loader
    override val packages: Set<String> = module.packages

    val configuration: Configuration = module.layer.configuration()
    val layer: ModuleLayer = module.layer
    private val services: Map<String, List<Class<*>>> = module.descriptor.provides().associate { it.service() to it.providers().map(classloader::loadClass) }

    override fun loadService(name: String): List<Class<*>> = services[name] ?: ArrayList()
}