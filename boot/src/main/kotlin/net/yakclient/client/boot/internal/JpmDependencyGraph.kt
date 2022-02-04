package net.yakclient.client.boot.internal

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.dep.Dependency
import net.yakclient.client.boot.dep.DependencyGraph
import net.yakclient.client.boot.dep.DependencyReference
import net.yakclient.client.boot.repository.RepositoryHandler
import java.lang.module.Configuration
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference
import java.nio.file.Path


internal class JpmDependencyGraph : DependencyGraph() {
    override fun <T : Dependency.Descriptor> ofRepository(handler: RepositoryHandler<T>): DependencyLoader<T> =
        JpmDependencyLoader(handler)

    private inner class JpmDependencyLoader<T : Dependency.Descriptor>(repo: RepositoryHandler<T>) :
        DependencyLoader<T>(repo) {
        override fun loadInternal(dep: Path, dependants: List<DependencyReference>): DependencyReference {
            val finder = ModuleFinder.of(dep)
            assert(finder.findAll().size == 1) { "Only able to load one dependency at a time!" }
            val reference = finder.findAll().first()

            assert(
                reference.descriptor().requires()
                    .filterNot { it.modifiers().contains(ModuleDescriptor.Requires.Modifier.STATIC) }
                    .all { r ->
                        fun Configuration.provides(name: String) : Boolean = modules().any { it.name() == name } || parents().any {it.provides(name)}

                        dependants.any { d -> r.name() == d.name || (d as JpmDependencyReference).configuration.provides(r.name()) } || ModuleLayer.boot().modules().any { d -> r.name() == d.name }
                    }) {
                "A Dependency of ${reference.descriptor().name()} is not in the graph!"
            }

            val references = dependants.filterIsInstance<JpmDependencyReference>().takeIf { it.size == dependants.size }
                ?: throw IllegalArgumentException("All dependants must be of type ${JpmDependencyReference::class.simpleName}")

            val configuration = Configuration.resolve(
                finder,
                references.map(JpmDependencyReference::configuration) + ModuleLayer.boot().configuration(),
                ModuleFinder.of(),
                finder.findAll().map(ModuleReference::descriptor).map(ModuleDescriptor::name)
            )

            val layer = ModuleLayer.defineModulesWithOneLoader(
                configuration,
                references.map(JpmDependencyReference::layer) + ModuleLayer.boot(),
                YakClient.loader
            ).layer()

            return JpmDependencyReference(layer.modules().first())
        }
    }
}