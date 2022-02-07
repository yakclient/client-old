package net.yakclient.client.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.*
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Path

public fun InputStream.readInputStream(): ByteArray = ByteArrayOutputStream().use { outputStream ->
    val size = available()

    val data = ByteArray(size)
    var bytesRead: Int
    var readCount = 0
    while (read(data, 0, size).also { bytesRead = it } != -1) {
        outputStream.write(data, 0, bytesRead)
        readCount++
    }

    outputStream.flush()

    if (readCount == 1) {
        data
    } else outputStream.toByteArray()
}

public fun URI.openStream(): InputStream = toURL().openStream()

public fun URL.isReachable(): Boolean = try {
    when(protocol) {
        "file" -> Files.exists(Path.of(file))
        else -> (openConnection() as? HttpURLConnection)?.responseCode == 200
    }
} catch (_: IOException) {
    false
}

public fun Path.createFile(): Boolean {
    if (parent.toFile().mkdirs()) return true
    return toFile().createNewFile()
}

public suspend infix fun URI.downloadTo(loc: Path): Path = runCatching {
    // TODO add some sort of assertion for making sure the file gets full downloaded(that's an issue that happens every once in a while and it totally breaks everything)

    Channels.newChannel(openStream()).use { cin ->
        withContext(Dispatchers.IO) {
            loc.createFile()
            FileOutputStream(loc.toFile()).use { fout ->
                fout.channel.transferFrom(cin, 0, Long.MAX_VALUE)
            }
        }

        return loc
    }
}.getOrNull() ?: throw IllegalStateException("Failed to download resource from location: ${toString()}")

public val URL.baseURL: String
    get() = this.protocol + ':' +
            (this.authority.takeUnless { it == null || it.isEmpty() }?.let { "//$it" } ?: "") +
            (this.path ?: "").removeSuffix("/")

public fun URL.resourceAt(path: String): URI = URI(("$baseURL/$path"))

public fun URL.urlAt(vararg paths: String): URL = URL(paths.fold(baseURL) { acc, it -> "$acc/$it" })

public fun Path.toUrl() : URL = toUri().toURL()

public fun Path.children() : List<Path> = toFile().listFiles()?.map { it.toPath() } ?: ArrayList()