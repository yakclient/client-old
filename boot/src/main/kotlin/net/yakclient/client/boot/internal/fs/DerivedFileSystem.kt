package net.yakclient.client.boot.internal.fs

import net.yakclient.client.boot.internal.volume.VolumeAwarePath
import java.nio.file.*
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider

internal class DerivedFileSystem(
    private val delegate: FileSystem,
    private val provider: FileSystemProvider,
    private val normalizer: PathNormalizer,
    private val root: String,
) : FileSystem() {
    override fun close(): Unit = delegate.close()

    override fun provider(): FileSystemProvider = provider

    override fun isOpen(): Boolean = delegate.isOpen

    override fun isReadOnly(): Boolean = delegate.isReadOnly

    override fun getSeparator(): String = delegate.separator

    override fun getRootDirectories(): MutableIterable<Path> = delegate.rootDirectories

    override fun getFileStores(): MutableIterable<FileStore> = delegate.fileStores

    override fun supportedFileAttributeViews(): MutableSet<String> = delegate.supportedFileAttributeViews()

    override fun getPath(first: String, vararg more: String?): Path {
        val basePath = delegate.getPath(first, *more)

        return VolumeAwarePath(delegate.getPath(root, basePath.toString()), this, normalizer)
    }

    override fun getPathMatcher(syntaxAndPattern: String?): PathMatcher = delegate.getPathMatcher(syntaxAndPattern)

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService = delegate.userPrincipalLookupService

    override fun newWatchService(): WatchService = delegate.newWatchService()
}