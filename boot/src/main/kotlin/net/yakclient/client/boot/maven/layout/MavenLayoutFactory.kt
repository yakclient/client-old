package net.yakclient.client.boot.maven.layout

import net.yakclient.archives.ArchiveCatalog
import net.yakclient.client.boot.maven.DEFAULT_MAVEN_LAYOUT
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.common.util.LazyMap

public object MavenLayoutFactory {
    private val providers: Map<String, MavenLayoutProvider> = LazyMap { key ->
        ArchiveCatalog.loadService(MavenLayoutProvider::class)
            .filter { it.provides(key) }
            .takeUnless { it.size > 1 }
            ?.firstOrNull()
            ?: throw IllegalStateException("There must be (only) 1 layout providers for layout: '$key'!")
    }

    private val layouts: Map<RepositorySettings, MavenRepositoryLayout> =
        LazyMap { providers[it.options["layout"] ?: DEFAULT_MAVEN_LAYOUT]!!.provide(it) }

    public fun createLayout(settings: RepositorySettings): MavenRepositoryLayout = layouts[settings]!!

}