package net.yakclient.client.util

import java.io.InputStream
import java.net.URI
import java.util.*

public fun URI.openStream(): InputStream = toURL().openStream()

public fun URI.open(): InputStream = toURL().openStream()

public fun URI.readBytes(): ByteArray = open().readInputStream()

public fun URI.readAsSha1(): ByteArray = HexFormat.of().parseHex(String(readBytes()).trim().subSequence(0, 40))

//private class WrappedClientStream(
//    delegate: InputStream,
//    private val client: CloseableHttpClient
//) : FilterInputStream(delegate) {
//    override fun close() {
//        client.close()
//        super.close()
//    }
//}