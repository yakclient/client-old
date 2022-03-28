package net.yakclient.client.util

import net.yakclient.client.util.resource.ExternalResource
import net.yakclient.client.util.resource.LocalResource
import net.yakclient.client.util.resource.SafeResource
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

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

public fun URI.openStream(): InputStream = toURL().openStream()

public fun URL.isReachable(): Boolean = try {
    when (protocol) {
        "file" -> Files.exists(Path.of(file))
        else -> (openConnection() as? HttpURLConnection)?.responseCode == 200
    }
} catch (_: IOException) {
    false
}

public fun Path.make(): Boolean =
    if (Files.isDirectory(this)) toFile().mkdirs()
    else {
        parent.toFile().mkdirs()
        toFile().createNewFile()
    }

public val URL.baseURL: String
    get() = this.protocol + ':' +
            (this.authority.takeUnless { it == null || it.isEmpty() }?.let { "//$it" } ?: "") +
            (this.path ?: "").removeSuffix("/")

public fun URL.uriAt(path: String): URI = URI(("$baseURL/$path"))

public fun URL.urlAt(vararg paths: String): URL = URL(paths.fold(baseURL) { acc, it -> "$acc/$it" })

public fun Path.toUrl(): URL = toUri().toURL()

public fun Path.children(): List<Path> = toFile().listFiles()?.map { it.toPath() } ?: ArrayList()

public infix fun Path.resolve(name: String): Path = resolve(name)

public infix fun Path.resolve(path: Path): Path = resolve(path)

private fun deleteAllInternal(path: Path): Boolean {
    path.children().forEach(::deleteAllInternal)

    return Files.deleteIfExists(path)
}

public fun Path.deleteAll(): Boolean = deleteAllInternal(this)

public fun URI.toResource(checkSum: ByteArray, checkType: String = "SHA1"): SafeResource =
    ExternalResource(this, checkSum, checkType)

public fun URI.open(): InputStream {
    val client =HttpClients.custom().build()
    val reqIn = client.execute(RequestBuilder.get().setUri(this).build()).entity.content

    return WrappedClientStream(reqIn, client)
}

private class WrappedClientStream(
    delegate: InputStream,
    private val client: CloseableHttpClient
) : FilterInputStream(delegate) {
    override fun close() {
        client.close()
        super.close()
    }
}


public fun URI.readBytes(): ByteArray = open().readInputStream()

public fun URI.readHexToBytes(): ByteArray = HexFormat.of().parseHex(String(readBytes()).trim())

public fun Path.toResource(): SafeResource = LocalResource(this)

public infix fun SafeResource.copyTo(to: Path): Path {
    Channels.newChannel(open()).use { cin ->
        to.make()
        FileOutputStream(to.toFile()).use { fout ->
            fout.channel.transferFrom(cin, 0, Long.MAX_VALUE)
        }
    }
    return to
}
