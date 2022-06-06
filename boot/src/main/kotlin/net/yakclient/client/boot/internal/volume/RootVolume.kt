package net.yakclient.client.boot.internal.volume

import net.yakclient.client.boot.container.ContainerVolume
import java.nio.file.Path

internal object RootVolume : ContainerVolume {
    override val name: String = "root"
    override val relativeRoot: Path = Path.of("/")
    override val parent: ContainerVolume? = null

    override fun derive(name: String, path: Path): ContainerVolume = DerivedVolume(name, path, RootVolume)
}