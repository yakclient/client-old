package net.yakclient.client.boot.internal.fs

import net.yakclient.client.boot.container.ContainerVolume
import net.yakclient.client.boot.container.callerContainer
import net.yakclient.client.boot.internal.volume.VolumeRelativePath
import net.yakclient.client.boot.internal.volume.absoluteRoot
import net.yakclient.common.util.resolve
import java.nio.file.*
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider

internal class VolumeAwareFileSystem(
    private val delegate: FileSystem,
) : FileSystem() {
    override fun close(): Unit = delegate.close()

    override fun provider(): FileSystemProvider = delegate.provider()

    override fun isOpen(): Boolean = delegate.isOpen

    override fun isReadOnly(): Boolean = delegate.isReadOnly

    override fun getSeparator(): String = delegate.separator

    override fun getRootDirectories(): MutableIterable<Path> = delegate.rootDirectories

    override fun getFileStores(): MutableIterable<FileStore> = delegate.fileStores

    override fun supportedFileAttributeViews(): MutableSet<String> = delegate.supportedFileAttributeViews()

    override fun getPath(first: String, vararg more: String?): Path {
        val basePath = delegate.getPath(first, *more)

        val container = callerContainer() ?: return basePath

        return container.volume.absoluteRoot() resolve basePath
    }

    override fun getPathMatcher(syntaxAndPattern: String?): PathMatcher = delegate.getPathMatcher(syntaxAndPattern)

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService = delegate.userPrincipalLookupService

    override fun newWatchService(): WatchService = delegate.newWatchService()
}