package net.yakclient.client.api.internal

import net.yakclient.archives.ArchiveHandle
import net.yakclient.client.boot.loader.ArchiveLoader
import net.yakclient.client.boot.loader.ClComponent

internal class MinecraftLoader(
    parent: ClassLoader,
//    providers: List<ConglomerateProvider>,
    components: List<ClComponent>,
    minecraft: ArchiveHandle,

//    private val nativeEnding: String,
//    private val natives: Map<String, Path>
) : ArchiveLoader(parent, components, minecraft) {
//    override fun findLibrary(libname: String): String {
//        return natives["lib$libname.$nativeEnding"]?.toAbsolutePath()?.toString() ?: super.findLibrary(libname)
//    }
//
//    override fun findResource(name: String): URL? {
//        return natives[name]?.toUri()?.toURL() ?: super.findResource(name)
//    }
}