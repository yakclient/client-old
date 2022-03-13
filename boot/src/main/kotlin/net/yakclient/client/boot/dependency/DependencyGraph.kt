package net.yakclient.client.boot.dependency

import net.yakclient.client.boot.exception.CyclicDependenciesException
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.LazyMap
import java.util.logging.Level
import java.util.logging.Logger


public object DependencyGraph {
    private val logger: Logger = Logger.getLogger(DependencyGraph::class.simpleName)

    private val graph: MutableMap<Dependency.Descriptor, DependencyNode> = HashMap()
//    private val loaders: Map<RepositorySettings, DependencyLoader<*>> =
//        LazyMap { DependencyLoader(RepositoryFactory.create(it)) }

    public fun ofRepository(settings: RepositorySettings): DependencyLoader<*> = ofRepository(RepositoryFactory.create(settings)) //loaders[settings]!!

    public fun <T : Dependency.Descriptor> ofRepository(handler: RepositoryHandler<T>): DependencyLoader<T> =
        DependencyLoader(handler)

    public open class DependencyLoader<T : Dependency.Descriptor>(
        private val repo: RepositoryHandler<T>,
        private val resolver: DependencyResolver = BasicDepResolver()
    ) {
        public fun load(name: String): ResolvedArchive? {
            return loadInternal(repo.loadDescription(name) ?: return null, null)?.reference
        }

        private fun loadInternal(desc: T, trace: DependencyTrace?): DependencyNode? {
            val name = desc.artifact

            logger.log(Level.INFO, "Loading dependency: ${desc.artifact}-${desc.version}")

            fun isCyclic(dt: DependencyTrace?): Boolean =
                dt?.desc?.artifact == name || (dt?.parent != null && isCyclic(dt.parent))

            fun topDependency(trace: DependencyTrace?): Dependency.Descriptor =
                if (trace == null) desc else if (trace.parent != null) topDependency(trace.parent) else trace.desc

            if (isCyclic(trace)) throw CyclicDependenciesException(topDependency(trace))

            val resolved = CachedDependency.Descriptor(desc.artifact, desc.version)

            return if (graph.contains(resolved) /* No need to check the dependency cache as the graph should have it if the graph does. */) graph[resolved]!!
            else {
                val dependency = repo.find(desc) ?: return null

                val cached = DependencyCache.cache(dependency)

                val children = dependency.dependants.mapTo(HashSet()) { d ->
                    d.possibleRepos.firstNotNullOfOrNull { r ->
                        DependencyLoader((RepositoryFactory.create(r) as RepositoryHandler<Dependency.Descriptor>), resolver).loadInternal(
                            d.desc,
                            DependencyTrace(trace, dependency.desc)
                        )
                    } ?: run {
                        logger.log(
                            Level.SEVERE, "Failed to find dependency: $d in trace: ${
                                trace?.flatten()?.asReversed()
                                    ?.joinToString(
                                        separator = " -> ",
                                        postfix = " -> $d"
                                    ) ?: "${dependency.desc.artifact} -> $d"
                            }"
                        )
                        return null
                    }
                }

                val dependencies = children.filterNot { c -> children.any { it.provides(c.desc) } }

                val reference = resolver(ArchiveUtils.find(cached.path), dependencies.map(DependencyNode::reference))

                val node = DependencyNode(cached.desc, reference, children)

                graph[cached.desc] = node

                node
            }
        }
    }
}

internal class DependencyNode(
    val desc: CachedDependency.Descriptor,
    val reference: ResolvedArchive,
    private val children: Set<DependencyNode>,
) {
    fun provides(other: Dependency.Descriptor): Boolean = children.any { it.desc == other || it.provides(other) }
}

internal data class DependencyTrace(
    val parent: DependencyTrace?,
    val desc: Dependency.Descriptor
) {
    fun flatten(): List<Dependency.Descriptor> = (parent?.flatten() ?: listOf()) + desc
}

