package net.yakclient.client.util.resource

import net.yakclient.client.util.openStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

public class LocalResource(
    override val uri: URI
) : SafeResource {
    init {
        assert(uri.host == null) { "Path not in local file system or does not exist! Uri in question '$uri'" }
    }

    public constructor(path: Path) : this(path.toUri())

    override fun open(): InputStream = BufferedInputStream(uri.openStream())
}