package net.yakclient.client.util

import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.ArrayList

public fun Path.make(): Boolean =
    if (Files.isDirectory(this)) toFile().mkdirs()
    else {
        parent.toFile().mkdirs()
        val it = runCatching { toFile().createNewFile() }
        if (it.isFailure) println(this)
        it.getOrThrow()
    }

public fun Path.toUrl(): URL = toUri().toURL()
public fun Path.children(): List<Path> = toFile().listFiles()?.map { it.toPath() } ?: ArrayList()
public infix fun Path.resolve(name: String): Path = resolve(name)
public infix fun Path.resolve(path: Path): Path = resolve(path)
public fun Path.deleteAll(): Boolean = deleteAllInternal(this)
private fun deleteAllInternal(path: Path): Boolean {
    path.children().forEach(::deleteAllInternal)

    return Files.deleteIfExists(path)
}