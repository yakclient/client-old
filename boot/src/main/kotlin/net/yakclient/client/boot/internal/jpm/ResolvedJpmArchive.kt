package net.yakclient.client.boot.internal.jpm

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.archive.ResolvedArchive
import java.lang.module.Configuration

internal class ResolvedJpmArchive(
    val module: Module, override val reference: ArchiveReference
) : ResolvedArchive {
    override val classloader: ClassLoader = module.classLoader ?: YakClient.loader
    override val name: String = module.name
    val configuration: Configuration = module.layer.configuration()
    val layer: ModuleLayer = module.layer
}