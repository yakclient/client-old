package net.yakclient.client.boot.dependency

import kotlinx.coroutines.*
import net.yakclient.archives.Archives
import net.yakclient.archives.ResolvedArchive
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.exception.CyclicDependenciesException
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.common.util.CAST
import net.yakclient.common.util.mapNotBlocking
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger

public object DependencyGraph {
    private val logger: Logger = Logger.getLogger(DependencyGraph::class.simpleName)
    public val defaultResolver: DependencyResolver = YakClient.dependencyResolver
    private val graph: MutableMap<Dependency.Descriptor, DependencyNode> = HashMap()
    private val defaultCache = DependencyCache(YakClient.settings.dependencyCacheLocation.toPath())

    public fun ofRepository(
        settings: RepositorySettings,
        resolver: DependencyResolver = defaultResolver,
        cache: DependencyCache = defaultCache
    ): DependencyLoader<*> = ofRepository(RepositoryFactory.create(settings), resolver, cache)

    public fun <T : Dependency.Descriptor> ofRepository(
        handler: RepositoryHandler<T>,
        resolver: DependencyResolver = defaultResolver,
        cache: DependencyCache = defaultCache,
    ): DependencyLoader<T> = DependencyLoader(handler, resolver, cache)

    public class DependencyLoader<D : Dependency.Descriptor> internal constructor(
        private val repo: RepositoryHandler<D>,
        private val resolver: DependencyResolver,
        private val cache: DependencyCache
    ) {
        public fun load(name: String, settings: DependencySettings = DependencySettings()): List<ResolvedArchive> {
            return load(
                repo.loadDescription(name) ?: throw IllegalArgumentException("Failed to parse dependency: $name"),
                settings
            )
        }

        public fun load(desc: D, settings: DependencySettings = DependencySettings()): List<ResolvedArchive> =
            runBlocking {
                val transaction = cache.Transaction()
                val cacheInternal = cacheInternal(desc, settings, transaction)

                if (cacheInternal) {
                    transaction.cache()
                    loadCached(cache.getOrNull(desc)!!).referenceOrChildren()
                } else {
                    transaction.rollback()
//                    logger.log(Level.WARNING, "Failed to cache dependency : '${desc.toPrettyString()}'")
                    listOf()
                }
            }

        private suspend fun cacheInternal(
            desc: D, settings: DependencySettings, transaction: DependencyCache.Transaction
        ): Boolean = cacheInternal(desc, settings, null, transaction)

        private suspend fun cacheInternal(
            desc: D, settings: DependencySettings, trace: DependencyTrace?, transaction: DependencyCache.Transaction
        ): Boolean {
            if (trace?.isCyclic(desc) == true) throw CyclicDependenciesException(trace.topDependency() ?: desc)

            val resolved = CachedDependency.Descriptor(desc.artifact, desc.version, desc.classifier)

            return if (!graph.contains(resolved) && !cache.contains(resolved)) {
                val dependency: Dependency = run {
                    val d = repo.find(desc) ?: return false

                    val transitiveDependencies = d.dependants.filterNotTo(HashSet()) {
                        settings.excludes.contains(it.desc.artifact)
                    }.let { if (settings.isTransitive) it else hashSetOf() }

                    Dependency(d.jar, transitiveDependencies, d.desc)
                }

                val jobs = dependency.dependants.mapNotBlocking { d ->
                    assert(d.possibleRepos.isNotEmpty()) { "Dependency: ${d.desc.toPrettyString()} has no associated repositories! (Issue in the repository handler: ${repo::class.java.name})" }

                    d.possibleRepos.any { r ->
                        val loader = (if (r == repo.settings) this else DependencyLoader(
                            RepositoryFactory.create(r), resolver, cache
                        )) as DependencyLoader<Dependency.Descriptor>

                        loader.cacheInternal(d.desc, settings, DependencyTrace(trace, dependency.desc), transaction)
                    } to d.desc
                }

                val failed = jobs.filterNot { r -> r.first }.map { it.second }

                if (failed.isNotEmpty()) {
                    logger.log(
                        Level.WARNING, "Failed to cache dependencies : ${
                            failed.joinToString(transform = Dependency.Descriptor::toPrettyString)
                        }, Dependency trace was: ${trace.toPrettyString(dependency)}. Looked into repository: ${repo.settings}"
                    )

                    false
                } else {
                    transaction.submit(dependency)
                    true
                }
            } else true
        }

        private fun loadCached(cached: CachedDependency): DependencyNode {
            val desc = cached.desc
            logger.log(Level.INFO, "Loading dependency: '${desc.toPrettyString()}'")

            if (graph.contains(desc)) return graph[desc]!!

            val cachedDeps: List<CachedDependency> =
                cached.dependants.map { cache.getOrNull(it) }.takeUnless { it.any { d -> d == null } }
                    ?.filterNotNull()
                    ?: throw IllegalStateException("Cached dependency: '${desc.toPrettyString()}' should already have all dependencies cached!")

            val children: Set<DependencyNode> = cachedDeps.mapTo(HashSet()) { loadCached(it) }

            val dependencies: List<DependencyNode> = children.filterNot { c -> children.any { it.provides(c.desc) } }

            val reference: ResolvedArchive? = if (cached.path != null) {
                val reference = runCatching { loadReference(cached.path, dependencies) }

                if (reference.isFailure) logger.log(
                    Level.SEVERE, "Failed to resolve dependency in trace : '${desc.toPrettyString()}'. Fatal error."
                )

                reference.getOrThrow()
            } else null

            return DependencyNode(desc, reference, children).also {
                graph[it.desc] = it
            }
        }

        private fun loadReference(
            path: Path, dependencies: List<DependencyNode>
        ) = resolver(
            Archives.find(path, Archives.Finders.JPM_FINDER),
            dependencies.flatMapTo(HashSet(), DependencyNode::referenceOrChildren)
        )
    }

    public data class DependencySettings(
        val isTransitive: Boolean = true,
        val excludes: Set<String> = hashSetOf(), // List of artifacts to exclude
    )
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
    separator = " -> ", postfix = " -> ${dependency.desc.toPrettyString()}"
) ?: dependency.desc.artifact

internal data class DependencyTrace(
    val parent: DependencyTrace?, val desc: Dependency.Descriptor
) {
    fun flatten(): List<Dependency.Descriptor> = (parent?.flatten() ?: listOf()) + desc

    fun depth(): Int = 1 + (parent?.depth() ?: 0)
}

public infix fun DependencyGraph.DependencyLoader<*>.load(name: String): List<ResolvedArchive> = this.load(name)

