package net.yakclient.client.boot.dependency

import net.yakclient.archives.ArchiveHandle
import net.yakclient.archives.ResolvedArchive


public fun interface DependencyResolver : (ArchiveHandle, Set<ResolvedArchive>) -> ResolvedArchive
