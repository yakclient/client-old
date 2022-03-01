package net.yakclient.client.boot.archive

import java.nio.file.Path
import java.util.*


public object ArchiveUtils {
    private val _finder: ArchiveFinder<*> = ServiceLoader.load(ArchiveFinder::class.java).firstOrNull()
        ?: throw IllegalStateException("Not able to load the ArchiveProvider, make sure all services are declared!")
    private val _resolver = ServiceLoader.load(ArchiveResolver::class.java)
        .firstOrNull()
        ?: throw IllegalStateException("Not able to load the ArchiveResolver, make sure all services are declared!")

    public fun find(path: Path, finder: ArchiveFinder<*> = _finder): ArchiveReference = finder.find(path)

    @JvmOverloads
    public fun <T : ArchiveReference> resolve(
        refs: List<T>,
        parents: List<ResolvedArchive> = ArrayList(),
        resolver: ArchiveResolver<T> = _resolver as ArchiveResolver<T>,
        clProvider: ClassLoaderProvider<T>,
    ): List<ResolvedArchive> =
        resolver.resolve(refs, clProvider, parents)

    @JvmOverloads
    public fun <T : ArchiveReference> resolve(
        ref: T,
        classloader: ClassLoader,
        parents: List<ResolvedArchive> = ArrayList(),
        resolver: ArchiveResolver<T> = _resolver as ArchiveResolver<T>,
    ): ResolvedArchive = resolve(listOf(ref), parents, resolver) { classloader }.first()
}