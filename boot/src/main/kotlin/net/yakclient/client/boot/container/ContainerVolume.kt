package net.yakclient.client.boot.container

import java.nio.file.Path

public interface ContainerVolume {
    public val name: String
    public val relativeRoot: Path
    public val parent: ContainerVolume?

    public fun derive(name: String, path: Path) : ContainerVolume
}
