package net.yakclient.client.api.internal

import net.yakclient.client.boot.loader.ClConglomerate
import net.yakclient.client.boot.loader.ConglomerateProvider
import java.net.URL
import java.nio.file.Path

internal class MinecraftLoader(
    parent: ClassLoader,
    providers: List<ConglomerateProvider>,
    private val nativeEnding: String,
    private val natives: Map<String, Path>
) : ClConglomerate(parent, providers) {
    override fun findLibrary(libname: String): String {
        return natives["lib$libname.$nativeEnding"]?.toAbsolutePath()?.toString() ?: super.findLibrary(libname)
    }

    override fun findResource(name: String): URL? {
        return natives[name]?.toUri()?.toURL() ?: super.findResource(name)
    }
}