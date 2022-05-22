package net.yakclient.client.boot.dependency

import net.yakclient.archives.ArchiveHandle
import net.yakclient.archives.Archives
import net.yakclient.archives.ResolvedArchive
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.loader.ArchiveComponent
import net.yakclient.client.boot.loader.ArchiveLoader

public open class ArchiveDependencyResolver : DependencyResolver {
    override fun invoke(archive: ArchiveHandle, dependants: List<ResolvedArchive>): ResolvedArchive {
        val loader = ArchiveLoader(YakClient.loader, dependants.map(::ArchiveComponent), archive)

       return Archives.resolve(archive, loader, parents = dependants)
    }
}