package net.yakclient.client.util

import java.io.*

public fun InputStream.readInputStream(): ByteArray = ByteArrayOutputStream().use { outputStream ->
    val buffer = ByteArrayOutputStream()

    var nRead: Int
    val data = ByteArray(4)

    while (read(data, 0, data.size).also { nRead = it } != -1) {
        buffer.write(data, 0, nRead)
    }

    buffer.flush()
    buffer.toByteArray()
}


