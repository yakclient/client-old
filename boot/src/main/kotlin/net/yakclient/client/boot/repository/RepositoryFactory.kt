package net.yakclient.client.boot.repository

import net.yakclient.common.util.LazyMap
import net.yakclient.common.util.ServiceListCollector

public object RepositoryFactory : ServiceListCollector<RepositoryProvider>() {
    private val handlers =
        LazyMap<RepositorySettings, RepositoryHandler<*>> { settings ->
            services.firstNotNullOfOrNull {
                it.provide(
                    settings
                ) ?: throw IllegalArgumentException("Failed to find repository for settings: $settings")
            }
        }

    public fun create(settings: RepositorySettings): RepositoryHandler<*> = handlers[settings]!!
}