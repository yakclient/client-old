package net.yakclient.client.boot.archive

import net.yakclient.client.boot.internal.ZipReference
import net.yakclient.client.boot.internal.ZipReferenceFinder
import net.yakclient.client.boot.internal.ZipReferenceResolver
import net.yakclient.client.boot.internal.jpm.JpmFinder
import net.yakclient.client.boot.internal.jpm.JpmReference
import net.yakclient.client.boot.internal.jpm.JpmResolver
import net.yakclient.client.util.CAST
import java.nio.file.Path
import java.util.*
import kotlin.reflect.KClass


public object ArchiveUtils {
    public val jpmResolver: ArchiveResolver<JpmReference> = JpmResolver()
    public val jpmFinder : ArchiveFinder<JpmReference> = JpmFinder()

    public val zipResolver : ArchiveResolver<ZipReference> = ZipReferenceResolver()
    public val zipFinder : ArchiveFinder<ZipReference> = ZipReferenceFinder()

//    @Suppress(CAST)
//    private fun <T : ArchiveReference> finder(clazz: KClass<T>): ArchiveFinder<T> {
//        return (ArchiveCatalog.loadService(ArchiveFinder::class).firstOrNull { clazz == it.type } as? ArchiveFinder<T>)
//            ?: throw IllegalStateException("Not able to load the ArchiveProvider, make sure all services are declared!")
//    }

    @Suppress(CAST)
    private fun <T : ArchiveReference> resolver(clazz: KClass<T>): ArchiveResolver<T> {
        return (ArchiveCatalog.loadService(ArchiveResolver::class)
            .firstOrNull { clazz == it.type } as? ArchiveResolver<T>)
            ?: throw IllegalStateException("Not able to load the ArchiveResolver, make sure all services are declared!")
    }

    public fun find(path: Path, finder: ArchiveFinder<*>): ArchiveReference = finder.find(path)

    @JvmOverloads
    public fun <T : ArchiveReference> resolve(
        refs: List<T>,
        resolver: ArchiveResolver<T> = run {
            val type = refs.first()::class
            check(refs.all { it::class == type }) { "All references must be of the same type!" }

            @Suppress(CAST)
            resolver(type) as ArchiveResolver<T>
        },
        parents: List<ResolvedArchive> = ArrayList(),
        clProvider: ClassLoaderProvider<T>,
    ): List<ResolvedArchive> = resolver.resolve(refs, clProvider, parents).onEach(ArchiveCatalog::catalog)

    @JvmOverloads
    public fun <T : ArchiveReference> resolve(
        ref: T,
        classloader: ClassLoader,
        resolver: ArchiveResolver<T> = @Suppress(CAST) (resolver(ref::class) as ArchiveResolver<T>),
        parents: List<ResolvedArchive> = ArrayList(),
    ): ResolvedArchive = resolve(listOf(ref), resolver, parents) { classloader }.first()
}