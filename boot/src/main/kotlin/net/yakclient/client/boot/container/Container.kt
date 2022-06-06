package net.yakclient.client.boot.container

import net.yakclient.client.boot.container.security.PrivilegeList

public data class Container(
    public val process: ContainerProcess,
    public val volume: ContainerVolume,
    public val privileges: PrivilegeList
)