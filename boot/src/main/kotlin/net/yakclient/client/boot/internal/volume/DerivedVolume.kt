package net.yakclient.client.boot.internal.volume

import net.yakclient.client.boot.container.ContainerVolume
import java.nio.file.Path

internal class DerivedVolume(
    override val name: String,
    override val relativeRoot: Path,
    override val parent: ContainerVolume?,
) : ContainerVolume {
    override fun derive(name: String, path: Path): ContainerVolume = DerivedVolume(name, path, this)
}