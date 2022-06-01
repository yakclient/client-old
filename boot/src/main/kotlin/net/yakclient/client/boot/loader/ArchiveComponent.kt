package net.yakclient.client.boot.loader

import net.yakclient.archives.ResolvedArchive

public class ArchiveComponent(
    private val archive: ResolvedArchive
) : ClComponent {
    override val packages: Set<String> = run {
        val visited = HashSet<ResolvedArchive>()

        fun parentPackages(archive: ResolvedArchive) : Set<String> = archive.packages + archive.parents.flatMap {
            if (visited.contains(it)) return@flatMap listOf()
            visited.add(it)

            parentPackages(it)
        }

        parentPackages(archive)
    }

    override fun loadClass(name: String): Class<*> = archive.classloader.loadClass(name)
}