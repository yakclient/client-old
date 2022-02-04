package net.yakclient.client.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.*
import java.nio.channels.Channels
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
    (openConnection() as HttpURLConnection).responseCode == 200
} catch (_: IOException) {
    false
}

public fun Path.createFile(): Boolean {
    if (parent.toFile().mkdirs()) return true
    return toFile().createNewFile()
}

public suspend infix fun URI.downloadTo(loc: Path): Path = runCatching {
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