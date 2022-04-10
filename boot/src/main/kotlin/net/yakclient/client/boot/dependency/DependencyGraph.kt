package net.yakclient.client.boot.dependency

import kotlinx.coroutines.*
import net.yakclient.client.boot.exception.CyclicDependenciesException
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.mapBlocking
import net.yakclient.client.util.mapNotBlocking
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger


public object DependencyGraph {
    private val logger: Logger = Logger.getLogger(DependencyGraph::class.simpleName)

    private val graph: MutableMap<Dependency.Descriptor, DependencyNode> = HashMap()

    public fun ofRepository(settings: RepositorySettings, resolver: DependencyResolver = ArchiveDependencyResolver()): DependencyLoader<*> =
        ofRepository(RepositoryFactory.create(settings), resolver)

    public fun <T : Dependency.Descriptor> ofRepository(handler: RepositoryHandler<T>, resolver: DependencyResolver): DependencyLoader<T> =
        DependencyLoader(handler, resolver)

    public class DependencyLoader<D : Dependency.Descriptor> internal constructor(
        private val repo: RepositoryHandler<D>,
        private val resolver: DependencyResolver = ArchiveDependencyResolver()
    ) {

        // TODO Replace the list with some sort of deferred archive that delegates to children
        public infix fun load(name: String): List<ResolvedArchive> {
            return load(repo.loadDescription(name) ?: return listOf())
        }

        private fun load(desc: D): List<ResolvedArchive> {
            val cacheInternal = runBlocking { cacheInternal(desc) }

            return if (cacheInternal) loadCached(DependencyCache.getOrNull(desc)!!).referenceOrChildren() else {
                logger.log(Level.WARNING, "Failed to cache dependency : '${desc.toPrettyString()}'")
                listOf()
            }
        }

        private suspend fun cacheInternal(desc: D): Boolean = cacheInternal(desc, null)

        // TODO add some sort of cache transaction that allows us to rollback any caching if something fails
        private suspend fun cacheInternal(desc: D, trace: DependencyTrace?): Boolean {
            if (trace?.isCyclic(desc) == true) throw CyclicDependenciesException(trace.topDependency() ?: desc)

            val resolved = CachedDependency.Descriptor(desc.artifact, desc.version)

            return if (!graph.contains(resolved) && !DependencyCache.contains(resolved)) {
                val dependency = repo.find(desc) ?: return false

                coroutineScope {
                    val jobs = dependency.dependants.mapNotBlocking { d ->
                        assert(d.possibleRepos.isNotEmpty()) { "Dependency: ${d.desc.toPrettyString()} has no associated repositories! (Issue in the repository handler: ${repo::class.java.name})" }

                        d.possibleRepos.any { r ->
                            val handler = RepositoryFactory.create(r) as RepositoryHandler<Dependency.Descriptor>
                            val loader = DependencyLoader(handler, resolver)

                            loader.cacheInternal(d.desc, DependencyTrace(trace, dependency.desc))
                        } to d.desc
                    }
//                    val jobs = dependency.dependants.map { d ->
//                        assert(d.possibleRepos.isNotEmpty()) { "Dependency: ${d.desc.toPrettyString()} has no associated repositories! (Issue in the repository handler: ${repo::class.java.name})" }
//
//                        async {
//                            d.possibleRepos.any { r ->
//                                val handler = RepositoryFactory.create(r) as RepositoryHandler<Dependency.Descriptor>
//                                val loader = DependencyLoader(handler, resolver)
//
//                                loader.cacheInternal(d.desc, DependencyTrace(trace, dependency.desc))
//                            } to d.desc
//                        }
//                    }

                    val failed = jobs
                        .filterNot { r -> r.first }
                        .map { it.second }

                    if (failed.isNotEmpty()) {
                        logger.log(
                            Level.WARNING,
                            "Failed caching dependencies : ${
                                failed.joinToString(transform = Dependency.Descriptor::toPrettyString)
                            }, Dependency trace was: ${trace.toPrettyString(dependency)}. "
                        )

                        false
                    } else {
                        DependencyCache.cache(dependency)
                        true
                    }
                }
            } else true
        }

        private fun loadCached(cached: CachedDependency): DependencyNode {
            val desc = cached.desc
            logger.log(Level.INFO, "Loading dependency: '${desc.toPrettyString()}'")

            if (graph.contains(desc)) return graph[desc]!!

            val cachedDeps: List<CachedDependency> = cached.dependants
                .map { DependencyCache.getOrNull(it) }
                .takeUnless { it.any { d -> d == null } }
                ?.filterNotNull()
                ?: throw IllegalStateException("Cached dependency: '${desc.toPrettyString()}' should already have all dependencies cached!")

            val children: Set<DependencyNode> = cachedDeps.mapTo(HashSet()) { loadCached(it) }

            val dependencies: List<DependencyNode> =
                children.filterNot { c -> children.any { it.provides(c.desc) } }

            val reference: ResolvedArchive? = if (cached.path != null) {
                val reference = runCatching { loadReference(cached.path, dependencies) }

                if (reference.isFailure) logger.log(
                    Level.SEVERE,
                    "Failed to resolve dependency in trace : '${desc.toPrettyString()}'. Fatal error."
                )

                reference.getOrThrow()
            } else null

            return DependencyNode(desc, reference, children).also {
                graph[it.desc] = it
            }
        }

        private fun loadReference(
            path: Path,
            dependencies: List<DependencyNode>
        ) = resolver(ArchiveUtils.find(path, ArchiveUtils.jpmFinder), dependencies.flatMap(DependencyNode::referenceOrChildren))
    }
}

private fun DependencyNode.referenceOrChildren(): List<ResolvedArchive> =
    this.reference?.let(::listOf) ?: this.children.flatMap { n ->
        n.reference?.let(::listOf) ?: n.referenceOrChildren()
    }

internal class DependencyNode(
    val desc: CachedDependency.Descriptor,
    val reference: ResolvedArchive?,
    val children: Set<DependencyNode>,
) {
    fun provides(other: Dependency.Descriptor): Boolean = children.any { it.desc == other || it.provides(other) }
}

private fun DependencyTrace.isCyclic(desc: Dependency.Descriptor): Boolean =
    (this.desc.artifact == desc.artifact) || ((this.parent != null) && this.parent.isCyclic(desc))

private fun DependencyTrace.topDependency(): Dependency.Descriptor? =
    if (this.parent != null) parent.topDependency() else this.desc

private fun DependencyTrace?.toPrettyString(
    dependency: Dependency
) = this?.flatten()?.joinToString(
        separator = " -> ",
        postfix = " -> ${dependency.desc.toPrettyString()}"
    ) ?: dependency.desc.artifact

internal data class DependencyTrace(
    val parent: DependencyTrace?,
    val desc: Dependency.Descriptor
) {
    fun flatten(): List<Dependency.Descriptor> = (parent?.flatten() ?: listOf()) + desc
}

