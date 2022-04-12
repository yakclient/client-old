package net.yakclient.client.util.resource

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI

public class ProvidedResource(override val uri: URI, private val bytes: ByteArray) : SafeResource {
    override fun open(): InputStream = ByteArrayInputStream(bytes)
}