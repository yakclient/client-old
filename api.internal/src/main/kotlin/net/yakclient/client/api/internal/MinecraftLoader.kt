package net.yakclient.client.api.internal

import net.yakclient.client.boot.loader.ClConglomerate
import net.yakclient.client.boot.loader.ConglomerateProvider
import java.nio.file.Path

internal class MinecraftLoader(
    parent: ClassLoader,
    providers: List<ConglomerateProvider>,
    private val natives : Map<String, Path>
) : ClConglomerate(parent, providers) {
    override fun findLibrary(libname: String): String {
        return natives[libname]?.toAbsolutePath()?.toString() ?: super.findLibrary(libname)
    }
}