package net.yakclient.client.boot.internal.zip

import net.yakclient.client.boot.archive.ArchiveResolver
import net.yakclient.client.boot.archive.ClassLoaderProvider
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.boot.loader.packageFormat
import kotlin.reflect.KClass

public class ZipResolver : ArchiveResolver<ZipHandle> {
    override val type: KClass<ZipHandle> = ZipHandle::class

    override fun resolve(
        archiveRefs: List<ZipHandle>,
        clProvider: ClassLoaderProvider<ZipHandle>,
        parents: List<ResolvedArchive>
    ): List<ResolvedArchive> = archiveRefs.associateBy(clProvider).map { entry ->
        val packages = entry.value.reader.entries()
            .map { it.name } // Mapping to the name of the entry
            .filter { it.endsWith(".class") } // Ensuring only java classes
            .filterNot { it == "module-info.class" } // Don't want the module-info
            .map { it.replace('/', '.').removeSuffix(".class") } // Normalizing names to appropriate class names
            .mapTo(HashSet()) { it.packageFormat } // Mapping to a package names within a HashSet

        ResolvedZip(entry.key, packages)
    }


}