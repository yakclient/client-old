package net.yakclient.client.boot.maven.layout

import net.yakclient.client.boot.maven.layout
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.common.util.LazyMap
import net.yakclient.common.util.ServiceListCollector

public object MavenLayoutFactory : ServiceListCollector<MavenLayoutProvider>( ){
    private val layouts: Map<RepositorySettings, MavenRepositoryLayout> =
        LazyMap { settings -> services.firstNotNullOfOrNull { it.provide(settings) } ?: throw IllegalArgumentException("Unable to find find layout for layout type: ${settings.layout}") }

    public fun createLayout(settings: RepositorySettings): MavenRepositoryLayout = layouts[settings]!!
}