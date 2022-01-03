package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.ext.ExtensionReference
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL

public open class ReferenceClassLoader(
    private val reference: ExtensionReference
) : ClassLoader() {
    override fun findResource(name: String): URL? =
        reference.load(name)?.asURI()?.toURL()

    override fun findClass(name: String): Class<*>? = reference.load(name)?.asInputStream()?.let {
        readInputStream(it).let { b -> defineClass(name, b, 0, b.size) }
    }

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
