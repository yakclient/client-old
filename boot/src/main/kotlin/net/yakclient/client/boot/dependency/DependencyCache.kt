package net.yakclient.client.boot.dependency

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.YakClient
import net.yakclient.common.util.copyTo
import net.yakclient.common.util.forEachBlocking
import net.yakclient.common.util.make
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

public class DependencyCache(
    private val cachePath : Path,
) {
    private val cacheMeta = cachePath.resolve("dependencies-meta.json")

    private val logger: Logger = Logger.getLogger(DependencyCache::class.simpleName)
    private val all: MutableMap<CachedDependency.Descriptor, CachedDependency>

    private val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    init {

        val metaFile = cacheMeta.toFile()

        if (cacheMeta.make()) metaFile.writeText(mapper.writeValueAsString(setOf<CachedDependency>()))

        all = mapper.readValue<Set<CachedDependency>>(metaFile).associateByTo(ConcurrentHashMap()) { it.desc }
    }

    public fun getOrNull(d: Dependency.Descriptor): CachedDependency? = all[CachedDependency.Descriptor(d.artifact, d.version, d.classifier)]

    public fun contains(d: Dependency.Descriptor): Boolean = all.contains(CachedDependency.Descriptor(d.artifact, d.version, d.classifier))

    public inner class Transaction {
        private val toCache: MutableList<Dependency> = ArrayList()

        public fun submit(dependency: Dependency) {
            toCache.add(dependency)
        }

        public fun rollback() {
            toCache.clear()
        }

        public suspend fun cache() {
            toCache.forEach { cache(it) }
        }

        private suspend fun cache(dependency: Dependency): CachedDependency {
            val desc = dependency.desc

            // Check if we need to cache the jar
            val cacheJar = dependency.jar != null

            // Create a cached descriptor
            val key = desc.let { CachedDependency.Descriptor(it.artifact, it.version, it.classifier) }
            // Check the in-memory cache to see if it has already been loaded, if it has then return it
            if (all.contains(key)) return all[key]!!

            // Create a path to where the artifact should be cached, if no version is present then making sure no extra '-' is included
            val jarPath = cachePath.resolve("${desc.artifact}${desc.version?.let { "-$it" } ?: ""}${desc.classifier?.let { "-$it" } ?: ""}.jar")

            // Creating the dependency to return.
            val cachedDependency = CachedDependency(
                jarPath.takeIf { cacheJar },

                // Mapping the dependencies to be pedantic
                dependency.dependants.map { CachedDependency.Descriptor(it.desc.artifact, it.desc.version, it.desc.classifier) },
                key
            )

            // If the file exists then don't overwrite it, at this point it should not exist.
            // If the jar is null then we dont have to do this, still important to update
            // meta.
            if (!Files.exists(jarPath) && cacheJar) {
                logger.log(Level.INFO, "Downloading dependency: ${desc.artifact}-${desc.version}")

                dependency.jar!! copyTo jarPath
            }

            // Getting all the cache dependencies
            val meta = all.values.toMutableSet()

            // Adding the current dependency to the ones we've already cached
            meta.add(cachedDependency)

            // Overwriting the meta file with the updated dependencies
            cacheMeta.toFile().writeText(mapper.writeValueAsString(meta))

            // Updating the in-memory cache
            all[cachedDependency.desc] = cachedDependency

            // Returning
            return cachedDependency
        }
    }
}