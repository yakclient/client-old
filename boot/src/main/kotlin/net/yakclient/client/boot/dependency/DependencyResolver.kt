package net.yakclient.client.boot.dependency

import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.archive.ResolvedArchive

public fun interface DependencyResolver : (ArchiveReference, List<ResolvedArchive>) -> ResolvedArchive
//{
//    public fun resolve(archive: ArchiveReference, dependants: List<ResolvedArchive>): ResolvedArchive
//}