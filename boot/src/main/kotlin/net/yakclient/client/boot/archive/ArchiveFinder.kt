package net.yakclient.client.boot.archive

import java.nio.file.Path
import kotlin.reflect.KClass

public interface ArchiveFinder<T : ArchiveReference> {
    public val type: KClass<T>

    public fun find(path: Path): T
}