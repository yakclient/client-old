package net.yakclient.client.boot.dependency

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.exception.CyclicDependenciesException
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.boot.loader.ArchiveComponent
import net.yakclient.client.boot.loader.ArchiveLoader
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import java.util.logging.Level
import java.util.logging.Logger


public object DependencyGraph {
    private val logger: Logger = Logger.getLogger(DependencyGraph::class.simpleName)

    private val graph: MutableMap<Dependency.Descriptor, DependencyNode> = HashMap()
    private val cache: DependencyCache = DependencyCache

    public fun ofRepository(settings: RepositorySettings): DependencyLoader<*> =
        ofRepository(RepositoryFactory.create(settings))

    public fun <T : Dependency.Descriptor> ofRepository(handler: RepositoryHandler<T>): DependencyLoader<T> =
        DependencyLoader(handler)

    public open class DependencyLoader<T : Dependency.Descriptor>(
        private val repo: RepositoryHandler<T>
    ) {
        public fun load(name: String): ResolvedArchive? {
            return loadInternal(repo.loadDescription(name) ?: return null, null)?.reference
        }

        private fun loadInternal(desc: T, trace: DependencyTrace?, ): DependencyNode? {
            val name = desc.artifact

            logger.log(Level.INFO, "Loading dependency: ${desc.artifact}-${desc.version}")

            fun isCyclic(dt: DependencyTrace?): Boolean =
                dt?.desc?.artifact == name || (dt?.parent != null && isCyclic(dt.parent))

            fun topDependency(trace: DependencyTrace?): Dependency.Descriptor =
                if (trace == null) desc else if (trace.parent != null) topDependency(trace.parent) else trace.desc

            if (isCyclic(trace)) throw CyclicDependenciesException(topDependency(trace))

            val resolved = CachedDependency.Descriptor(desc.artifact, desc.version)

            return if (cache.isCached(resolved) && graph.contains(resolved)) graph[resolved]
            else {
                val dependency = repo.find(desc) ?: return null

                val cached = cache.cache(dependency)

                val children = dependency.dependants.mapTo(HashSet()) {
                    loadInternal(it as T, DependencyTrace(trace, dependency.desc)) ?: run {
                        logger.log(
                            Level.SEVERE, "Failed to find dependency: $it in trace: ${
                                trace?.flatten()?.asReversed()
                                    ?.joinToString(
                                        separator = " -> ",
                                        postfix = " -> $it"
                                    ) ?: "${dependency.desc.artifact} -> $it"
                            }"
                        )
                        return null
                    }
                }

                val dependencies = children.filterNot { c -> children.any { it.provides(c.desc) } }

                val reference = resolve(ArchiveUtils.find(cached.path), dependencies.map(DependencyNode::reference))

                val node = DependencyNode(cached.desc, reference, children)

                graph[cached.desc] = node

                node
            }
        }

        public open fun resolve(archive: ArchiveReference, dependants: List<ResolvedArchive>): ResolvedArchive {
            val loader = ArchiveLoader(YakClient.loader, dependants.map(::ArchiveComponent), archive)

            return ArchiveUtils.resolve(archive, loader, dependants)
        }
    }
}

internal class DependencyNode(
    val desc: CachedDependency.Descriptor,
    val reference: ResolvedArchive,
    private val children: Set<DependencyNode>,
) {
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as DependencyNode
//
//        if (desc != other.desc) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int = desc.hashCode()

    fun provides(other: Dependency.Descriptor): Boolean = children.any { it.desc == other || it.provides(other) }
}

internal data class DependencyTrace(
    val parent: DependencyTrace?,
    val desc: Dependency.Descriptor
) {
    fun flatten(): List<Dependency.Descriptor> = (parent?.flatten() ?: listOf()) + desc
}

//public fun Dependency.Descriptor.toResolveDesc(): CachedDependency.Descriptor =
//    CachedDependency.Descriptor(artifact, version)

//private fun DependencyNode.toResolveDesc(): CachedDependency.Descriptor = desc.toResolveDesc()

//private fun loadMeta(): Map<String, CachedDependency> {
//    val metaFile = cacheMeta.toFile()
//
//    if (cacheMeta.createFile()) mapOf<String, CachedDependency>().toConfig(META_ROOT_NAME)
//        .writeTo(metaFile)
//
//    return ConfigFactory.parseFile(metaFile).extract(META_ROOT_NAME)
//}

