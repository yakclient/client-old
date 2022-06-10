package net.yakclient.client.boot.container

import net.yakclient.client.boot.container.security.Privileges
import net.yakclient.client.boot.container.volume.ContainerVolume

public data class Container(
    public val process: ContainerProcess,
    public val volume: ContainerVolume,
    public val privileges: Privileges
)