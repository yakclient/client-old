package net.yakclient.client.boot.container

import net.yakclient.client.boot.container.security.PrivilegeList
import net.yakclient.client.boot.loader.ArchiveComponent
import net.yakclient.client.boot.loader.ArchiveSourceProvider
import net.yakclient.client.boot.loader.ContainerClassLoader

public object ContainerLoader {
    public fun <T : ContainerInfo> load(
        info: T,
        loader: ProcessLoader<T>,
        volume: ContainerVolume,
        privileges: PrivilegeList,
        parent: ClassLoader,
    ): Container {
        val handle = ContainerHandle()

        val cl = ContainerClassLoader(ArchiveSourceProvider(info.handle), privileges, ContainerSource(handle), info.dependencies.map(::ArchiveComponent), parent)

        val container = Container(loader.load(info, cl), volume, privileges)
        handle.handle = container

        return container
    }
}