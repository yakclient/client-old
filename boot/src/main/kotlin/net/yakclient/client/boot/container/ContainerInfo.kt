package net.yakclient.client.boot.container

import net.yakclient.archives.ArchiveHandle
import net.yakclient.archives.ResolvedArchive

public interface ContainerInfo {
    public val handle: ArchiveHandle
    public val dependencies: Set<ResolvedArchive>
}
