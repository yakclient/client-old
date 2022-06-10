package net.yakclient.client.boot.internal.volume

import net.yakclient.client.boot.container.volume.ContainerVolume
import net.yakclient.common.util.resolve
import java.nio.file.FileSystem
import java.nio.file.Path

internal class DerivedVolume(
    override val name: String,
    override val relativeRoot: Path,
    override val parent: ContainerVolume?,
    override val fs: FileSystem,
) : ContainerVolume {
    override fun derive(name: String, path: Path): ContainerVolume = DerivedVolume(name, path, this, /*fs.provider().newFileSystem((absoluteRoot() resolve path).toUri(), mapOf<String, Any>()) */ fs)
}