package net.yakclient.client.boot.dependency

import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.archive.ResolvedArchive

public class DependencyResolutionFallBack(
    private val fallback: DependencyResolver,
    private val call: (ArchiveReference, List<ResolvedArchive>) -> ResolvedArchive?,
) : DependencyResolver {
    override fun invoke(ref: ArchiveReference, dependants: List<ResolvedArchive>): ResolvedArchive =
        call(ref, dependants) ?: fallback(ref, dependants)

}