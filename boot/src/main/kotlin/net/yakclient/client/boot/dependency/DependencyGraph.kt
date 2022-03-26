package net.yakclient.client.boot.dependency

import net.yakclient.client.boot.exception.CyclicDependenciesException
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger


public object DependencyGraph {
    private val logger: Logger = Logger.getLogger(DependencyGraph::class.simpleName)

    private val graph: MutableMap<Dependency.Descriptor, DependencyNode> = HashMap()

    public fun ofRepository(settings: RepositorySettings): DependencyLoader<*> =
        ofRepository(RepositoryFactory.create(settings))

    public fun <T : Dependency.Descriptor> ofRepository(handler: RepositoryHandler<T>): DependencyLoader<T> =
        DependencyLoader(handler)

    public class DependencyLoader<T : Dependency.Descriptor> internal constructor(
        private val repo: RepositoryHandler<T>,
        private val resolver: DependencyResolver = BasicDepResolver()
    ) {
        public fun load(name: String): ResolvedArchive? {
            return load(repo.loadDescription(name) ?: return null)?.reference
        }

        private fun load(desc: T): DependencyNode? {
            return if (cacheInternal(desc, null)) loadCached(DependencyCache.getOrNull(desc) ?: return null)
            else null
        }

        private fun cacheInternal(desc: T, trace: DependencyTrace?): Boolean {

            if (trace?.isCyclic(desc) == true) throw CyclicDependenciesException(trace.topDependency() ?: desc)

            val resolved = CachedDependency.Descriptor(desc.artifact, desc.version)

            if (!graph.contains(resolved)) {
                if (DependencyCache.contains(resolved)) return true

                val dependency = repo.find(desc) ?: return false

                dependency.dependants.forEach { d ->
                    assert(d.possibleRepos.isNotEmpty()) { "Dependency: ${d.desc.artifact} has no associated repositories! (Issue in the repository handler: ${repo::class.java})" }
                    d.possibleRepos.firstNotNullOfOrNull { r ->
                        val handler = RepositoryFactory.create(r) as RepositoryHandler<Dependency.Descriptor>
                        val loader = DependencyLoader(handler, resolver)

                        loader.cacheInternal(d.desc, DependencyTrace(trace, dependency.desc))
                    } ?: run {
                        logger.log(
                            Level.SEVERE,
                            "Failed to find dependency: $d in trace: ${trace.toPrettyString(d, dependency)}"
                        )

                        return false
                    }
                }

                DependencyCache.cache(dependency)
                return true
            } else return true
        }


        private fun loadCached(cached: CachedDependency): DependencyNode {
            val desc = cached.desc
            logger.log(Level.INFO, "Loading dependency: ${desc.artifact}-${desc.version}")

            val cachedDeps: List<CachedDependency> = cached.dependants
                .map { DependencyCache.getOrNull(it) }
                .takeUnless { it.any { d -> d == null } }
                ?.filterNotNull() ?: throw IllegalStateException("Cached dependency: '$desc' ")

            val children: Set<DependencyNode> = cachedDeps.mapTo(HashSet()) { graph[it.desc] ?: loadCached(it) }

            val dependencies: List<DependencyNode> =
                children.filterNot { c -> children.any { it.provides(c.desc) } }

            val reference: ResolvedArchive? =
                if (cached.path != null) loadReference(cached.path, dependencies) else null

            return DependencyNode(desc, reference, children).also {
                graph[it.desc] = it
            }
        }

        private fun loadReference(
            path: Path,
            dependencies: List<DependencyNode>
        ) = resolver(ArchiveUtils.find(path), dependencies.flatMap {
            fun DependencyNode.referenceOrChildren(): List<ResolvedArchive> =
                this.reference?.let(::listOf) ?: this.children.flatMap { n ->
                    n.reference?.let(::listOf) ?: n.referenceOrChildren()
                }

            it.referenceOrChildren()
        })
    }


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
    d: Dependency.Transitive,
    dependency: Dependency
) = this?.flatten()?.asReversed()
    ?.joinToString(
        separator = " -> ",
        postfix = " -> $d"
    ) ?: "${dependency.desc.artifact} -> $d"

internal data class DependencyTrace(
    val parent: DependencyTrace?,
    val desc: Dependency.Descriptor
) {
    fun flatten(): List<Dependency.Descriptor> = (parent?.flatten() ?: listOf()) + desc
}

