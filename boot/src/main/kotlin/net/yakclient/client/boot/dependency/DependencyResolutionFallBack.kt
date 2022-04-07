package net.yakclient.client.boot.dependency

import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.archive.ResolvedArchive

public abstract class DependencyResolutionFallBack(
    private val fallback: DependencyResolver,
) : DependencyResolver {
    override fun invoke(ref: ArchiveReference, dependants: List<ResolvedArchive>): ResolvedArchive =
        resolve(ref, dependants) ?: fallback(ref, dependants)

    public abstract fun resolve(ref: ArchiveReference, dependants: List<ResolvedArchive>): ResolvedArchive?
}

public fun DependencyResolver.resolveOrFallBack(call: (ref: ArchiveReference, dependants: List<ResolvedArchive>) -> ResolvedArchive?) : DependencyResolver = object: DependencyResolutionFallBack(this) {
    override fun resolve(ref: ArchiveReference, dependants: List<ResolvedArchive>): ResolvedArchive? = call(ref, dependants)
}
