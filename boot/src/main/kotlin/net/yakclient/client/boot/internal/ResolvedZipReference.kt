package net.yakclient.client.boot.internal

import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.archive.ResolvedArchive

public class ResolvedZipReference(
    override val classloader: ClassLoader, override val reference: ArchiveReference
) : ResolvedArchive {
    override fun loadService(name: String): Nothing = throw UnsupportedOperationException("Loading services from ResolvedArchiveReference is not supported")
}