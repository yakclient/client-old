package net.yakclient.client.boot.dependency

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.archive.ArchiveHandle
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.boot.loader.ArchiveComponent
import net.yakclient.client.boot.loader.ArchiveLoader

public open class ArchiveDependencyResolver : DependencyResolver {
    override fun invoke(archive: ArchiveHandle, dependants: List<ResolvedArchive>): ResolvedArchive {
        val loader = ArchiveLoader(YakClient.loader, dependants.map(::ArchiveComponent), archive)

       return ArchiveUtils.resolve(archive, loader, parents = dependants)
    }
}