package net.yakclient.client.boot.repository

import java.util.*

public object RepositoryFactory {
    private val providers: Map<RepositoryType, RepositoryProvider> =
        object : EnumMap<RepositoryType, RepositoryProvider>(RepositoryType::class.java) {
            override fun get(key: RepositoryType): RepositoryProvider =
                if (containsKey(key)) super.get(key)!! else (ServiceLoader.load(RepositoryProvider::class.java)
                    .filter { it.provides(key) }.takeUnless { it.size > 1 }?.firstOrNull()?.also { put(key, it) }
                    ?: throw IllegalStateException("Must be (only) 1 repository providers per type!"))
        }

    public fun create(settings: RepositorySettings): RepositoryHandler<*> = providers[settings.type]!!.provide(settings)
}