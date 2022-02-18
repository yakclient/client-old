package net.yakclient.client.boot.dep

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.github.config4k.toConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.yakclient.client.boot.YakClient
import net.yakclient.client.util.make
import net.yakclient.client.util.downloadTo
import net.yakclient.client.util.writeTo
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

internal object DependencyCache {
    private const val META_ROOT_NAME = "dependency-meta"

    private val cachePath = YakClient.settings.dependencyCacheLocation.toPath()
    private val cacheMeta = cachePath.resolve("dependencies-meta.conf")

    private val logger: Logger = Logger.getLogger(DependencyCache::class.simpleName)
    private val all: MutableMap<CachedDependency.Descriptor, CachedDependency>

    init {
        val metaFile = cacheMeta.toFile()

        if (cacheMeta.make()) setOf<CachedDependency>().toConfig(META_ROOT_NAME).writeTo(metaFile)

        all = ConfigFactory.parseFile(metaFile).extract<Set<CachedDependency>>(META_ROOT_NAME)
            .associateByTo(ConcurrentHashMap()) { it.desc }
    }

    fun cache(dependency: CachedDependency) {
        all[dependency.desc] = dependency
    }

    fun resolveAll(dependencies: Set<Dependency.Descriptor>): Pair<Set<CachedDependency>, Set<Dependency.Descriptor>> =
        all.keys.let { dependencies.intersect(it).mapNotNullTo(HashSet(), all::get) to dependencies.subtract(it) }

    fun isCached(descriptor: Dependency.Descriptor) = all.contains(descriptor)

    // TODO add checksum support
    fun cache(dependency: Dependency): CachedDependency = runBlocking {
        if (all.contains(dependency.desc)) return@runBlocking all[dependency.desc]!!

        val desc = dependency.desc
        val jarPath = cachePath.resolve("${desc.artifact}${desc.version?.let { "-$it" } ?: ""}.jar")
        val cachedDependency = CachedDependency(
            jarPath,
            dependency.dependants.map { CachedDependency.Descriptor(it.artifact, it.version) },
            CachedDependency.Descriptor(desc.artifact, desc.version)
        )

        if (!Files.exists(jarPath)) launch(Dispatchers.IO) {
            logger.log(Level.INFO, "Downloading dependency: ${desc.artifact}-${desc.version}")

            dependency.uri downloadTo jarPath
        }

        launch(Dispatchers.IO) {
            val meta = all.values.toMutableSet() //all.mapKeysTo(HashMap()) { (key, _) -> "\"${key.artifact}:${key.version}\"" }

            if (!isCached(desc)) {
                meta.add(cachedDependency)

                meta.toConfig(META_ROOT_NAME).writeTo(cacheMeta.toFile())
            }
//            "\"${desc.artifact}:${desc.version}\"".takeUnless { meta.contains(it) }?.also { a ->
//
//                meta[a] = cachedDependency
//
//                meta.toConfig(META_ROOT_NAME).writeTo(cacheMeta.toFile())
//            }

            all[cachedDependency.desc] = cachedDependency
        }


        cachedDependency
    }
}

//internal class CachedRepositoryHandler : RepositoryHandler<CachedDependency.Descriptor> {
//    private val meta: Map<String, CachedDependency>
//    override val settings: RepositorySettings
//        get() = throw UnsupportedOperationException("Settings not supported in this context")
//
//    init {
//        val metaFile = cacheMeta.toFile()
//
//        if (cacheMeta.createFile()) mapOf<String, CachedDependency>().toConfig(META_ROOT_NAME).writeTo(metaFile)
//
//        meta = ConfigFactory.parseFile(metaFile).extract(META_ROOT_NAME)
//    }
//
//    override fun find(desc: CachedDependency.Descriptor): Dependency? = meta[desc.artifact]?.let { Dependency(it.path.toUri(), it.dependants, it.desc) }
//
//    override fun loadDescription(dep: String): CachedDependency.Descriptor? =
//        meta[dep]?.desc as? CachedDependency.Descriptor
//}
