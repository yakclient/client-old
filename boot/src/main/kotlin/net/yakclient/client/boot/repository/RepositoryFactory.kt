package net.yakclient.client.boot.repository

import net.yakclient.client.boot.archive.ArchiveCatalog
import net.yakclient.client.util.LazyMap
import java.util.*

public object RepositoryFactory {
    private val providers = LazyMap<String, RepositoryProvider> { key ->
       ArchiveCatalog.loadService(RepositoryProvider::class)
            .filter { it.provides(key) }
            .takeUnless { it.size > 1 }
            ?.firstOrNull()
            ?: throw IllegalStateException("There must be (only) 1 repository providers for repo type '$key'!")
    }

    private val handlers = LazyMap<RepositorySettings, RepositoryHandler<*>> { settings -> providers[settings.type]!!.provide(settings) }

    public fun create(settings: RepositorySettings): RepositoryHandler<*> = handlers[settings]!!
}