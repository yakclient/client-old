package net.yakclient.client.boot.internal.volume

import net.yakclient.client.boot.container.ContainerVolume
import java.nio.file.Path

internal fun ContainerVolume.absoluteRoot(): Path = parent?.let { VolumeRelativePath(relativeRoot, it) } ?: relativeRoot