package net.yakclient.client.boot.archive

import java.nio.file.Path
import java.util.*
import kotlin.reflect.full.isSuperclassOf

public object ArchiveUtils {
    private val provider: ArchiveFinder<*> = ServiceLoader.load(ArchiveFinder::class.java).firstOrNull()
        ?: throw IllegalStateException("Not able to load the ArchiveProvider, make sure all services are declared!")
    private val resolver: ArchiveResolver<ArchiveReference> =
        ServiceLoader.load(ArchiveResolver::class.java).firstOrNull() as? ArchiveResolver<ArchiveReference>
            ?: throw IllegalStateException("Not able to load the ArchiveResolver, make sure all services are declared!")

    public fun find(path: Path): ArchiveReference = provider.find(path)

    public fun resolve(reference: ArchiveReference, parents: List<ResolvedArchive>): ResolvedArchive {
        check(resolver.accepts.isSuperclassOf(reference::class))
        return resolver.resolve(reference, parents)
    }
}