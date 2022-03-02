package net.yakclient.client.util.resource

import java.io.InputStream
import java.net.URI

public interface SafeResource {
    public val uri: URI

    public fun open() : InputStream
}