package net.yakclient.client.boot.internal.fs

import net.yakclient.client.boot.container.callerContainer
import net.yakclient.client.boot.internal.volume.absoluteRoot
import net.yakclient.common.util.resolve
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider

public class VolumeAwareFileSystemProvider(
    private val delegate: FileSystemProvider
) : FileSystemProvider() {
    private val fs = VolumeAwareFileSystem(delegate.getFileSystem(URI.create("/")))
//    private val fsByVolume = LazyMap<URI, FileSystem> {
//        val volume = VolumeManager.getVolume(it)
//        if (volume != null) VolumeAwareFileSystem(volume, getFileSystem(URI.create("/"))) else null
//    }

    override fun getScheme(): String = delegate.scheme

    override fun newFileSystem(uri: URI?, env: MutableMap<String, *>?): FileSystem = delegate.newFileSystem(uri, env)
//        throw FileSystemAlreadyExistsException("Cannot create new file system, must create a new volume!")

    override fun getFileSystem(uri: URI): FileSystem = fs
//        fsByVolume[uri] ?: throw IllegalArgumentException("Volume at: '$uri' does not exist!")

    override fun getPath(uri: URI): Path {
        val basePath = delegate.getPath(uri)

        val container = callerContainer() ?: return basePath

        return container.volume.absoluteRoot() resolve basePath
    }

    override fun newByteChannel(
        path: Path?,
        options: MutableSet<out OpenOption>?,
        vararg attrs: FileAttribute<*>?
    ): SeekableByteChannel = delegate.newByteChannel(path, options, *attrs)

    override fun newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path> =
        delegate.newDirectoryStream(dir, filter)

    override fun createDirectory(dir: Path?, vararg attrs: FileAttribute<*>?): Unit =
        delegate.createDirectory(dir, *attrs)

    override fun delete(path: Path?): Unit = delegate.delete(path)

    override fun copy(source: Path?, target: Path?, vararg options: CopyOption?): Unit =
        delegate.copy(source, target, *options)

    override fun move(source: Path?, target: Path?, vararg options: CopyOption?): Unit =
        delegate.move(source, target, *options)

    override fun isSameFile(path: Path?, path2: Path?): Boolean = delegate.isSameFile(path, path2)

    override fun isHidden(path: Path?): Boolean = delegate.isHidden(path)

    override fun getFileStore(path: Path?): FileStore = delegate.getFileStore(path)

    override fun checkAccess(path: Path?, vararg modes: AccessMode?): Unit = delegate.checkAccess(path, *modes)

    override fun <V : FileAttributeView?> getFileAttributeView(
        path: Path?,
        type: Class<V>?,
        vararg options: LinkOption?
    ): V = delegate.getFileAttributeView(path, type, *options)

    override fun <A : BasicFileAttributes?> readAttributes(
        path: Path?,
        type: Class<A>?,
        vararg options: LinkOption?
    ): A = delegate.readAttributes(path, type, *options)

    override fun readAttributes(
        path: Path?,
        attributes: String?,
        vararg options: LinkOption?
    ): MutableMap<String, Any> = delegate.readAttributes(path, attributes, *options)

    override fun setAttribute(path: Path?, attribute: String?, value: Any?, vararg options: LinkOption?): Unit =
        delegate.setAttribute(path, attribute, value, *options)
}