package net.yakclient.client.boot.internal.volume

import net.yakclient.client.boot.container.volume.ContainerVolume
import net.yakclient.common.util.resolve
import java.nio.file.Path

internal fun ContainerVolume.absoluteRoot(): Path =
    parent?.let { it.absoluteRoot() resolve relativeRoot } ?: relativeRoot