package net.yakclient.client.boot.extension

import net.yakclient.archives.ArchiveHandle
import net.yakclient.archives.ResolvedArchive
import net.yakclient.client.boot.container.ContainerInfo

public data class ExtensionInfo(
    override val handle: ArchiveHandle,
    public val parent: Extension,
    public val settings: ExtensionSettings = ExtensionLoader.loadSettings(handle),
    override val dependencies: Set<ResolvedArchive> = ExtensionLoader.loadDependencies(settings),
) : ContainerInfo
