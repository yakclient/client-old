package net.yakclient.client.boot.internal.zip

import net.yakclient.client.boot.archive.ResolvedArchive

public class ResolvedZip(
    override val classloader: ClassLoader//, override val reference: ArchiveReference
    , override val packages: Set<String>
) : ResolvedArchive {
//    override val packages: Set<String> =

    override fun loadService(name: String): Nothing = throw UnsupportedOperationException("Loading services from ResolvedArchiveReference is not supported")
}