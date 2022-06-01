package net.yakclient.client.api.internal

import net.yakclient.archives.ArchiveHandle
import net.yakclient.client.boot.loader.ArchiveLoader
import net.yakclient.client.boot.loader.ClComponent

internal class MinecraftLoader(
    parent: ClassLoader,
    components: List<ClComponent>,
    minecraft: ArchiveHandle,
) : ArchiveLoader(parent, components, minecraft)