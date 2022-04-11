package net.yakclient.client.boot.dependency

import net.yakclient.client.boot.archive.ArchiveHandle
import net.yakclient.client.boot.archive.ResolvedArchive

public fun interface DependencyResolver : (ArchiveHandle, List<ResolvedArchive>) -> ResolvedArchive
