package net.yakclient.client.extension

import net.yakclient.client.boot.container.Container

public interface ContainerGroup {
    public val name: String
    public val containers: List<Container>
}