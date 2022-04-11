package net.yakclient.client.boot.internal.zip

import net.yakclient.client.boot.archive.ArchiveFinder
import java.nio.file.Path
import java.util.jar.JarFile
import java.util.zip.ZipFile
import kotlin.reflect.KClass

public class ZipFinder : ArchiveFinder<ZipHandle> {
    override val type: KClass<ZipHandle> = ZipHandle::class

    override fun find(path: Path): ZipHandle {
        return ZipHandle(
            JarFile(
                path.toFile().also { assert(it.exists()) },
                true,
                ZipFile.OPEN_READ,
                JarFile.runtimeVersion()
            ), path.toUri()
        )
    }
}