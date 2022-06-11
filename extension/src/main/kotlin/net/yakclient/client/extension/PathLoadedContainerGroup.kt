package net.yakclient.client.extension

import net.yakclient.client.boot.container.Container
import net.yakclient.client.boot.extension.Extension
import net.yakclient.common.util.children
import java.nio.file.Path

public open class PathLoadedContainerGroup(
    override val name: String,
    protected val path: Path,
    protected val parent: Extension
) : ContainerGroup {
    override val containers: List<Container> = path.children()
        .filter { it.fileName.toString().endsWith(".jar") }
        .map { MinecraftExtensionLoader.load(it, parent) }
}