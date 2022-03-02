package net.yakclient.client.util.resource

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI
import java.nio.file.Path

public class LocalResource(
    private val path: Path
) : SafeResource {
    override val uri: URI = path.toUri()

    override fun open(): InputStream = BufferedInputStream(FileInputStream(path.toFile()))
}