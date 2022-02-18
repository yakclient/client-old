package net.yakclient.client.boot.internal.jpm

import net.yakclient.client.boot.archive.ArchiveFinder
import java.lang.module.ModuleFinder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory

internal class JpmFinder : ArchiveFinder<JpmReference> {
    override fun find(path: Path): JpmReference {
        assert(!Files.isDirectory(path)) { "Cannot load directory of archives from path: $path" }
        return JpmReference(
            ModuleFinder.of(path).findAll()?.firstOrNull()
                ?: throw IllegalArgumentException("Failed to find archives in path: $path")
        )
    }
}

//internal class JpmFinder(
//    private val finder: ModuleFinder
//) : ArchiveFinder<JpmReference>, ModuleFinder {
//    private val archives: Map<String, JpmReference> = finder.findAll().map{ JpmReference(it) }.associateBy { it.descriptor().name() }
//
//    override fun allArchives(): Set<JpmReference> = archives.values.toSet()
//
//    override fun findArchive(name: String): JpmReference = JpmReference(
//        finder.find(name).orElseGet(null)
//            ?: throw IllegalArgumentException("Failed to find archive: $name in finder: $finder")
//    )
//
//    override fun find(name: String): Optional<ModuleReference> = Optional.of(findArchive(name))
//
//    override fun findAll(): Set<ModuleReference> = allArchives()
//
//    class Provider : ArchiveFinder.Provider {
//        override fun provide(path: Path): JpmFinder = JpmFinder(ModuleFinder.of(path))
//    }
//}