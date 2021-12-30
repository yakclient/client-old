package net.yakclient.client.internal.lifecycle

import net.yakclient.client.internal.extension.ExtensionReference
import java.io.ByteArrayOutputStream
import java.io.InputStream

public open class ModuleReferenceClassLoader(
    private val reference: ExtensionReference
) : ClassLoader() {
    override fun findClass(name: String): Class<*> = readInputStream(
        reference.load(name)?.asInputStream() ?: throw ClassNotFoundException("Failed to find class \"$name\"")
    ).let { defineClass(name, it, 0, it.size) }

    //TODO clean up a little
    private fun readInputStream(ips: InputStream): ByteArray = ByteArrayOutputStream().use { outputStream ->
        val size = ips.available()

        val data = ByteArray(size)
        var bytesRead: Int
        var readCount = 0
        while (ips.read(data, 0, size).also { bytesRead = it } != -1) {
            outputStream.write(data, 0, bytesRead)
            readCount++
        }

        outputStream.flush()

        if (readCount == 1) {
            data
        } else outputStream.toByteArray()
    }
}
