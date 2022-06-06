package net.yakclient.client.extension.test

import java.io.File
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider
import kotlin.test.Test
import kotlin.test.assertTrue

class TestFileSystemOverloading {
    @Test
    fun `Test file system creation`() {
        val delegateSys = FileSystems.getDefault()
        println(delegateSys)

        val path = Path.of("")

        path.register(FileSystems.getDefault().newWatchService(), )

        delegateSys.provider().getFileSystem(URI.create("/Users/durgan/IdeaProjects/yakclient/client"))
//        println(MyFileSystem(delegateSys).rootDirectories.map { it.toString() })
    }

    class MyFileSystemProvider(
        private val delegate: FileSystemProvider
    ) : FileSystemProvider() {
        override fun getScheme(): String = delegate.scheme

        // THIS ONE
        override fun newFileSystem(uri: URI?, env: MutableMap<String, *>?): FileSystem =
            MyFileSystem(delegate.newFileSystem(uri, env))

        // THIS ONE
        override fun getFileSystem(uri: URI?): FileSystem = MyFileSystem(delegate.getFileSystem(uri))

        override fun getPath(uri: URI): Path = delegate.getPath(uri)

        override fun newByteChannel(
            path: Path?,
            options: MutableSet<out OpenOption>?,
            vararg attrs: FileAttribute<*>?
        ): SeekableByteChannel = delegate.newByteChannel(path, options, *attrs)

        override fun newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path> =
            delegate.newDirectoryStream(dir, filter)

        override fun createDirectory(dir: Path?, vararg attrs: FileAttribute<*>?) =
            delegate.createDirectory(dir, *attrs)

        override fun delete(path: Path?) = delegate.delete(path)

        override fun copy(source: Path?, target: Path?, vararg options: CopyOption?) =
            delegate.copy(source, target, *options)

        override fun move(source: Path?, target: Path?, vararg options: CopyOption?) =
            delegate.move(source, target, *options)

        override fun isSameFile(path: Path?, path2: Path?): Boolean = delegate.isSameFile(path, path2)

        override fun isHidden(path: Path?): Boolean = delegate.isHidden(path)

        override fun getFileStore(path: Path?): FileStore = delegate.getFileStore(path)

        override fun checkAccess(path: Path?, vararg modes: AccessMode?) = delegate.checkAccess(path, *modes)

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

        override fun setAttribute(path: Path?, attribute: String?, value: Any?, vararg options: LinkOption?) =
            delegate.setAttribute(path, attribute, value, *options)
    }

    private class MyFileSystem(
        private val delegate: FileSystem
    ) : FileSystem() {
        override fun close() = delegate.close()

        override fun provider(): FileSystemProvider = delegate.provider()

        override fun isOpen(): Boolean = delegate.isOpen

        override fun isReadOnly(): Boolean = delegate.isReadOnly

        override fun getSeparator(): String = delegate.separator

        // THIS ONE
        override fun getRootDirectories(): MutableIterable<Path> = delegate.rootDirectories


        override fun getFileStores(): MutableIterable<FileStore> = delegate.fileStores

        override fun supportedFileAttributeViews(): MutableSet<String> = delegate.supportedFileAttributeViews()

        override fun getPath(first: String, vararg more: String?): Path = delegate.getPath(first, *more)

        override fun getPathMatcher(syntaxAndPattern: String?): PathMatcher = delegate.getPathMatcher(syntaxAndPattern)

        override fun getUserPrincipalLookupService(): UserPrincipalLookupService = delegate.userPrincipalLookupService

        override fun newWatchService(): WatchService = delegate.newWatchService()
    }
}