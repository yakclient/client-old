package net.yakclient.client.boot.loader

import net.yakclient.archives.ResolvedArchive

public class ArchiveComponent(
    private val archive: ResolvedArchive
) : ClComponent {
    private fun allPackages(archive: ResolvedArchive): Set<String> =
        archive.packages + archive.parents.flatMap(::allPackages)

    override val packages: Set<String> = allPackages(archive)

    override fun loadClass(name: String): Class<*> = archive.classloader.loadClass(name)
}