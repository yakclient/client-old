package net.yakclient.client.boot.archive

import net.yakclient.client.util.transformable
import java.util.ServiceLoader
import kotlin.reflect.KClass

public object ArchiveCatalog {
    private val _archives: MutableSet<ResolvedArchive> = HashSet()
    public val archives: Set<ResolvedArchive> by transformable(::_archives) { it.toSet() /* Make sure its immutable */ }

    internal fun catalog(archive: ResolvedArchive) = _archives.add(archive)

    public fun <T : Any> loadService(service: KClass<T>): List<T> = loadService(service.java)

    public fun <T : Any> loadService(service: Class<T>): List<T> =
        ServiceLoader.load(service).toList() + archives.flatMap { it.loadService(service.name) }
            .map { it.getConstructor().newInstance() }
            .map { it as T }
//                archives.map { ServiceLoader.load(service, it.classloader) }.flatMap { it.toList() }

//        val allSet = HashSet<String>()
//        val services = ArrayList<T>()
//
//        for (t in all)  if (allSet.add(t::class.java.name)) services.add(t)
//
//        return services
//}

}