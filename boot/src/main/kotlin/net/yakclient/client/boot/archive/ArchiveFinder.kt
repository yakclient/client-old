package net.yakclient.client.boot.archive

import java.nio.file.Path

public interface ArchiveFinder<out T : ArchiveReference> {
    public fun find(path: Path): T
}