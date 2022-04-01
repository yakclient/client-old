package net.yakclient.client.util

import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import java.io.FilterInputStream
import java.io.InputStream
import java.net.URI
import java.util.*

public fun URI.openStream(): InputStream = toURL().openStream()

public fun URI.open(): InputStream {
    val client = HttpClients.custom().build()
    val reqIn = client.execute(RequestBuilder.get().setUri(this).build()).entity.content

    return WrappedClientStream(reqIn, client)
}

public fun URI.readBytes(): ByteArray = open().readInputStream()

public fun URI.readAsSha1(): ByteArray = HexFormat.of().parseHex(String(readBytes()).trim().subSequence(0, 40))

private class WrappedClientStream(
    delegate: InputStream,
    private val client: CloseableHttpClient
) : FilterInputStream(delegate) {
    override fun close() {
        client.close()
        super.close()
    }
}