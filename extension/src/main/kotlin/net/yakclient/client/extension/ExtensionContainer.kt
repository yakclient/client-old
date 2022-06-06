package net.yakclient.client.extension

import net.yakclient.archives.ResolvedArchive

public data class ExtensionContainer(
    public val archive: ResolvedArchive,
    public val volume: ExtensionVolume
)