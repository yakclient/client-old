package net.yakclient.client.boot.container

import net.yakclient.common.util.immutableLateInit

public class ContainerHandle {
    public var handle: Container by immutableLateInit()
}
