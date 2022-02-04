package net.yakclient.client.boot.internal

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.dep.DependencyReference
import java.lang.module.Configuration

internal class JpmDependencyReference(
    val module: Module
) : DependencyReference {
    override val classloader: ClassLoader = module.classLoader ?: YakClient.loader
    override val name: String = module.name
    val configuration = module.layer.configuration()
    val layer = module.layer
}