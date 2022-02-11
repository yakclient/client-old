package net.yakclient.client.boot.archive

import kotlin.reflect.KClass

public interface ArchiveResolver<T : ArchiveReference> {
    public val accepts: KClass<T>

    public fun resolve(ref: T, parents: List<ResolvedArchive>): ResolvedArchive
}