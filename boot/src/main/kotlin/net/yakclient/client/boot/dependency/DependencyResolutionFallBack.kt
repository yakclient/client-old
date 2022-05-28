package net.yakclient.client.boot.dependency

import net.yakclient.archives.ArchiveHandle
import net.yakclient.archives.ResolvedArchive


public abstract class DependencyResolutionFallBack(
    private val fallback: DependencyResolver,
) : DependencyResolver {
    override fun invoke(ref: ArchiveHandle, dependants: Set<ResolvedArchive>): ResolvedArchive =
        resolve(ref, dependants) ?: fallback(ref, dependants)

    public abstract fun resolve(ref: ArchiveHandle, dependants: Set<ResolvedArchive>): ResolvedArchive?
}

public fun DependencyResolver.resolveOrFallBack(call: (ref: ArchiveHandle, dependants: Set<ResolvedArchive>) -> ResolvedArchive?) : DependencyResolver = object: DependencyResolutionFallBack(this) {
    override fun resolve(ref: ArchiveHandle, dependants: Set<ResolvedArchive>): ResolvedArchive? = call(ref, dependants)
}
