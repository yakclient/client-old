package net.yakclient.client.boot.maven

import net.yakclient.client.boot.archive.ArchiveCatalog
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.LazyMap

public object MavenLayoutFactory {
    private val providers: Map<String, MavenLayoutProvider> = LazyMap { key ->
        ArchiveCatalog.loadService(MavenLayoutProvider::class)
            .filter { it.provides(key) }
            .takeUnless { it.size > 1 }
            ?.firstOrNull()
            ?: throw IllegalStateException("There must be (only) 1 layout providers for layout: '$key'!")
    }

    private val layouts: Map<RepositorySettings, MavenRepositoryLayout> =
        LazyMap { providers[it.options["layout"] ?: "default"]!!.provide(it) }

    public fun createLayout(settings: RepositorySettings): MavenRepositoryLayout = layouts[settings]!!

}