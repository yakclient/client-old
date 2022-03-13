package net.yakclient.client.boot.dependency

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.YakClient
import net.yakclient.client.util.copyTo
import net.yakclient.client.util.make
import net.yakclient.client.util.downloadTo
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

internal object DependencyCache {
    private const val META_ROOT_NAME = "dependency-meta"

    private val cachePath = YakClient.settings.dependencyCacheLocation.toPath()
    private val cacheMeta = cachePath.resolve("dependencies-meta.json")

    private val logger: Logger = Logger.getLogger(DependencyCache::class.simpleName)
    private val all: MutableMap<CachedDependency.Descriptor, CachedDependency>

    private val mapper: ObjectMapper = XmlMapper().registerModule(KotlinModule())

    init {
        val metaFile = cacheMeta.toFile()

        if (cacheMeta.make()) metaFile.writeText( mapper.writeValueAsString(setOf<CachedDependency>()))
//        if (cacheMeta.make()) setOf<CachedDependency>().toConfig(META_ROOT_NAME).writeTo(metaFile)

        all = mapper.readValue<Set<CachedDependency>>(metaFile).associateByTo(ConcurrentHashMap()) { it.desc }
//        all = ConfigFactory.parseFile(metaFile).extract<Set<CachedDependency>>(META_ROOT_NAME)
//            .associateByTo(ConcurrentHashMap()) { it.desc }
    }

    fun cache(dependency: CachedDependency) {
        all[dependency.desc] = dependency
    }

    fun resolveAll(dependencies: Set<Dependency.Descriptor>): Pair<Set<CachedDependency>, Set<Dependency.Descriptor>> =
        all.keys.let { dependencies.intersect(it).mapNotNullTo(HashSet(), all::get) to dependencies.subtract(it) }

    fun isCached(descriptor: Dependency.Descriptor) = all.contains(descriptor.let { CachedDependency.Descriptor(it.artifact, it.version) })

    // TODO add checksum support
    fun cache(dependency: Dependency): CachedDependency {// runBlocking {
        val key = dependency.desc.let { CachedDependency.Descriptor(it.artifact, it.version) }
        if (all.contains(key)) return all[key]!!

        val desc = dependency.desc
        val jarPath = cachePath.resolve("${desc.artifact}${desc.version?.let { "-$it" } ?: ""}.jar")
        val cachedDependency = CachedDependency(
            jarPath,
            dependency.dependants.map { CachedDependency.Descriptor(it.desc.artifact, it.desc.version) },
            key
        )

        if (!Files.exists(jarPath)) /*launch(Dispatchers.IO)*/ {
            logger.log(Level.INFO, "Downloading dependency: ${desc.artifact}-${desc.version}")

            dependency.jar copyTo jarPath
        }

//        launch(Dispatchers.IO) {
            val meta = all.values.toMutableSet() //all.mapKeysTo(HashMap()) { (key, _) -> "\"${key.artifact}:${key.version}\"" }

            if (!isCached(desc)) {
                meta.add(cachedDependency)

                cacheMeta.toFile().writeText(mapper.writeValueAsString(meta))
            }
//            "\"${desc.artifact}:${desc.version}\"".takeUnless { meta.contains(it) }?.also { a ->
//
//                meta[a] = cachedDependency
//
//                meta.toConfig(META_ROOT_NAME).writeTo(cacheMeta.toFile())
//            }

            all[cachedDependency.desc] = cachedDependency
//        }


        return cachedDependency
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
