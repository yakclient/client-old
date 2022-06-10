package net.yakclient.client.boot.internal.fs

import net.yakclient.client.boot.container.callerContainer
import net.yakclient.client.boot.internal.volume.VolumeAwarePath
import net.yakclient.common.util.LazyMap
import java.net.URI
import java.nio.channels.FileChannel
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider

public class VolumeAwareFileSystemProvider(
    private val delegate: FileSystemProvider
) : FileSystemProvider() {
    private val delegateFS = delegate.getFileSystem(URI.create("file:/"))

    private val normalizer: PathNormalizer = PathNormalizer {
        if (it is VolumeAwarePath) delegateFS.getPath(it.toString()) else it
    }
    private val systems = LazyMap<String, FileSystem> {
        if (it == "/" || it =="") VolumeAwareFileSystem(delegateFS, this, normalizer) else DerivedFileSystem(delegateFS, this, normalizer, it)
    }
    private val rootSystem = systems["/"]!!

    override fun getScheme(): String = delegate.scheme

    override fun newFileSystem(uri: URI, env: MutableMap<String, *>?): FileSystem =
        systems[uri.path] ?: delegate.newFileSystem(uri, env)

    override fun getFileSystem(uri: URI): FileSystem = systems[uri.path]!!

    override fun getPath(uri: URI): Path {
        return rootSystem.getPath(uri.path)
//        val basePath = delegate.getPath(uri)
//
//        val container = callerContainer() ?: return VolumeAwarePath(basePath, rootSystem, normalizer)
//
//        return container.volume.fs.getPath(uri.path)
    }

    override fun newByteChannel(
        path: Path?,
        options: MutableSet<out OpenOption>?,
        vararg attrs: FileAttribute<*>?
    ): SeekableByteChannel = delegate.newByteChannel(path?.let(normalizer::normalize), options, *attrs)

    override fun newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path> =
        delegate.newDirectoryStream(dir?.let(normalizer::normalize), filter)

    override fun createDirectory(dir: Path?, vararg attrs: FileAttribute<*>?): Unit =
        delegate.createDirectory(dir?.let(normalizer::normalize), *attrs)

    override fun delete(path: Path?): Unit = delegate.delete(path?.let(normalizer::normalize))

    override fun copy(source: Path?, target: Path?, vararg options: CopyOption?): Unit =
        delegate.copy(source?.let(normalizer::normalize), target, *options)

    override fun move(source: Path?, target: Path?, vararg options: CopyOption?): Unit =
        delegate.move(source?.let(normalizer::normalize), target, *options)

    override fun isSameFile(path: Path?, path2: Path?): Boolean =
        delegate.isSameFile(path?.let(normalizer::normalize), path2)

    override fun isHidden(path: Path?): Boolean = delegate.isHidden(path?.let(normalizer::normalize))

    override fun getFileStore(path: Path?): FileStore = delegate.getFileStore(path?.let(normalizer::normalize))

    override fun checkAccess(path: Path?, vararg modes: AccessMode?): Unit =
        delegate.checkAccess(path?.let(normalizer::normalize), *modes)

    override fun <V : FileAttributeView?> getFileAttributeView(
        path: Path?,
        type: Class<V>?,
        vararg options: LinkOption?
    ): V = delegate.getFileAttributeView(path?.let(normalizer::normalize), type, *options)

    override fun <A : BasicFileAttributes?> readAttributes(
        path: Path?,
        type: Class<A>?,
        vararg options: LinkOption?
    ): A = delegate.readAttributes(path?.let(normalizer::normalize), type, *options)

    override fun readAttributes(
        path: Path?,
        attributes: String?,
        vararg options: LinkOption?
    ): MutableMap<String, Any> = delegate.readAttributes(path?.let(normalizer::normalize), attributes, *options)

    override fun setAttribute(path: Path?, attribute: String?, value: Any?, vararg options: LinkOption?): Unit =
        delegate.setAttribute(path?.let(normalizer::normalize), attribute, value, *options)

    override fun newFileChannel(
        path: Path?,
        options: MutableSet<out OpenOption>?,
        vararg attrs: FileAttribute<*>?
    ): FileChannel {
        return delegate.newFileChannel(path?.let(normalizer::normalize), options, *attrs)
    }
}