package net.yakclient.client.boot.internal.fs

import java.nio.file.Path

public fun interface PathNormalizer {
    public fun normalize(path: Path) : Path
}