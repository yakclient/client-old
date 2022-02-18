package net.yakclient.client.boot.archive

public fun interface ClassLoaderProvider<R: ArchiveReference> : (R) -> ClassLoader

public interface ArchiveResolver<T : ArchiveReference> {
    public fun resolve(refs: List<T>, clProvider: ClassLoaderProvider<T>, parents: List<ResolvedArchive>): List<ResolvedArchive>
}