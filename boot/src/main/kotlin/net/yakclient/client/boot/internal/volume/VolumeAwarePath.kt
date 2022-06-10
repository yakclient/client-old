package net.yakclient.client.boot.internal.volume

import net.yakclient.client.boot.internal.fs.PathNormalizer
import java.io.File
import java.net.URI
import java.nio.file.*
import java.nio.file.spi.FileSystemProvider

//public fun VolumeRelativePath(relativePath: Path, volume: ContainerVolume): Path = volume.absoluteRoot().resolve(relativePath)

//internal class VolumeRelativePath(
//    private val relativePath: Path,
//    private val volume: ContainerVolume
//) : Path by (volume.absoluteRoot() resolve relativePath)

internal class VolumeAwarePath(
    private val delegatePath: Path,
    private val fs : FileSystem,
    private val normalizer: PathNormalizer
) : Path by delegatePath {
    override fun getRoot(): Path? = delegatePath.root?.let { VolumeAwarePath(it, fs, normalizer) }

    override fun getFileName(): Path? = delegatePath.fileName?.let { VolumeAwarePath(it, fs, normalizer) }

    override fun getParent(): Path? = delegatePath.parent?.let { VolumeAwarePath(it, fs, normalizer) }

    override fun getName(index: Int): Path = VolumeAwarePath(delegatePath.getName(index), fs, normalizer)

    override fun subpath(beginIndex: Int, endIndex: Int): Path =
        VolumeAwarePath(delegatePath.subpath(beginIndex, endIndex), fs, normalizer)

    override fun startsWith(other: Path): Boolean = delegatePath.startsWith(normalizer.normalize(other))

    override fun endsWith(other: Path): Boolean = delegatePath.endsWith(normalizer.normalize(other))

    override fun normalize(): Path = VolumeAwarePath(delegatePath.normalize(), fs, normalizer)

    override fun resolve(other: Path): Path {
        return VolumeAwarePath(fs.getPath(delegatePath.toString(), other.toString()), fs, normalizer)
    }

    override fun relativize(other: Path): Path =
        VolumeAwarePath(delegatePath.relativize(normalizer.normalize(other)), fs, normalizer)

    override fun toAbsolutePath(): Path = VolumeAwarePath(delegatePath.toAbsolutePath(), fs, normalizer)

    override fun toRealPath(vararg options: LinkOption?): Path =
        VolumeAwarePath(delegatePath.toRealPath(*options), fs, normalizer)

    override fun toFile(): File = File(toString())

    override fun getFileSystem(): FileSystem = fs

    override fun toString(): String = delegatePath.toString()

    override fun compareTo(other: Path): Int = delegatePath.compareTo(normalizer.normalize(other))

    override fun equals(other: Any?): Boolean = delegatePath == other

    override fun hashCode(): Int = delegatePath.hashCode()
}

