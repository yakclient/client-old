package net.yakclient.client.extension

import java.nio.file.Path

public interface ExtensionVolume {
    public val parent: ExtensionVolume?
    public val relativeRoot: Path

    public fun derive(root: Path) : ExtensionVolume
}