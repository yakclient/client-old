package net.yakclient.client.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.yakclient.client.boot.container.Container
import net.yakclient.client.boot.extension.Extension
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchEvent
import java.util.logging.Level
import java.util.logging.Logger

// TODO make configurable
private const val WATCH_DELAY: Long = 5000

public class PathWatchingContainerGroup(
    name: String,
    path: Path,
    parent: Extension
) : PathLoadedContainerGroup(
    name,
    path,
    parent
) {
    private val watcher = path.fileSystem.newWatchService()
    private val logger = Logger.getLogger(this::class.simpleName)
    private val mutableContainers = super.containers.toMutableList()
    override val containers: List<Container>
        get() = mutableContainers.toList()

    init {
        path.register(watcher, ENTRY_CREATE) // TODO ENTRY_DELETE, ENTRY_MODIFY

        val scope = CoroutineScope(Dispatchers.Default)

        scope.launch {
            while (true) {
                delay(WATCH_DELAY)

                val key = watcher.poll() ?: continue

                for (event in key.pollEvents()) {
                    if (event.kind() == OVERFLOW) continue

                    val filePath = (event as WatchEvent<Path>).context().toAbsolutePath()
                    if (!filePath.fileName.toString().endsWith(".jar")) {
                        logger.log(Level.INFO, "New file creation detected in directory: '$path'. File name is: '${filePath.toFile()}' but suffix is not '.jar' so it is not loadable!")
                        continue
                    }

                    mutableContainers.add(MinecraftExtensionLoader.load(filePath, parent))
                }
            }
        }
    }
}