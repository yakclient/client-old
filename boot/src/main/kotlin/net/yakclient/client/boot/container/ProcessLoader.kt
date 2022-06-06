package net.yakclient.client.boot.container

public interface ProcessLoader<I: ContainerInfo> {
    public fun load(info: I, loader: ClassLoader) : ContainerProcess
}