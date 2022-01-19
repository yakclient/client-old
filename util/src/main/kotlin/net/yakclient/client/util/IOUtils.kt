package net.yakclient.client.util

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.*

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