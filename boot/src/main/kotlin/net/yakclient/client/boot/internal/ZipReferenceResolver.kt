package net.yakclient.client.boot.internal

import net.yakclient.client.boot.archive.ArchiveResolver
import net.yakclient.client.boot.archive.ClassLoaderProvider
import net.yakclient.client.boot.archive.ResolvedArchive
import kotlin.reflect.KClass

public class ZipReferenceResolver : ArchiveResolver<ZipReference> {
    override val type: KClass<ZipReference> = ZipReference::class

    override fun resolve(
        archiveRefs: List<ZipReference>,
        clProvider: ClassLoaderProvider<ZipReference>,
        parents: List<ResolvedArchive>
    ): List<ResolvedArchive> = archiveRefs.associateBy(clProvider).map { ResolvedZipReference(it.key, it.value) }
}