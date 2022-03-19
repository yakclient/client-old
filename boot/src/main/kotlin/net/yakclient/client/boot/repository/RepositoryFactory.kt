package net.yakclient.client.boot.repository

import net.yakclient.client.util.LazyMap
import java.util.*

public object RepositoryFactory {
    private val providers = LazyMap<String, RepositoryProvider> { key ->
        ServiceLoader.load(RepositoryProvider::class.java)
            .filter { it.provides(key) }
            .takeUnless { it.size > 1 }
            ?.firstOrNull()
            ?: throw IllegalStateException("There must be (only) 1 repository providers for repo type '$key'!")
    }

    private val handlers = LazyMap<RepositorySettings, RepositoryHandler<*>> { settings -> providers[settings.type]!!.provide(settings) }

//    private val providers: Map<RepositoryType, RepositoryProvider> =
//        object : EnumMap<RepositoryType, RepositoryProvider>(RepositoryType::class.java) {
//            override fun get(key: RepositoryType): RepositoryProvider =
//                if (containsKey(key)) super.get(key)!! else (ServiceLoader.load(RepositoryProvider::class.java)
//                    .filter { it.provides(key) }.takeUnless { it.size > 1 }?.firstOrNull()?.also { put(key, it) }
//                    ?: throw IllegalStateException("Must be (only) 1 repository providers per type!"))
//        }

    public fun create(settings: RepositorySettings): RepositoryHandler<*> = handlers[settings]!!
}