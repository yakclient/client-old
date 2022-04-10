package net.yakclient.client.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import net.yakclient.client.util.resource.ExternalResource
import net.yakclient.client.util.resource.LocalResource
import net.yakclient.client.util.resource.SafeResource
import java.io.FileOutputStream
import java.net.URI
import java.nio.channels.Channels
import java.nio.file.Path

public fun URI.toResource(checkSum: ByteArray, checkType: String = "SHA1"): SafeResource =
    ExternalResource(this, checkSum, checkType)

public fun Path.toResource(): SafeResource = LocalResource(this)

public suspend infix fun SafeResource.copyTo(to: Path): Path = coroutineScope { // Dont want to use IO scope as we need to make sure the resource has been fully copied before returning
    Channels.newChannel(open()).use { cin ->
        to.make()
        FileOutputStream(to.toFile()).use { fout ->
            fout.channel.transferFrom(cin, 0, Long.MAX_VALUE)
        }
    }
    to
}