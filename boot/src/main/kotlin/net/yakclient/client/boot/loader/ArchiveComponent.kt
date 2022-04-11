package net.yakclient.client.boot.loader

import net.yakclient.client.boot.archive.ResolvedArchive

public class ArchiveComponent(
    private val archive: ResolvedArchive
) : ClComponent {
    override val packages: Set<String> by archive::packages

    override fun loadClass(name: String): Class<*> = archive.classloader.loadClass(name)
}