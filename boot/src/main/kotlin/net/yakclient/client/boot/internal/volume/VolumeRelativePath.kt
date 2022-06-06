package net.yakclient.client.boot.internal.volume

import net.yakclient.client.boot.container.ContainerVolume
import net.yakclient.common.util.resolve
import java.nio.file.Path

public fun VolumeRelativePath(relativePath: Path, volume: ContainerVolume): Path = volume.absoluteRoot().resolve(relativePath)

//internal class VolumeRelativePath(
//    private val relativePath: Path,
//    private val volume: ContainerVolume
//) : Path by (volume.absoluteRoot() resolve relativePath)