package net.yakclient.client.boot.ext

import io.github.config4k.extract
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.dep.DependencyReference
import net.yakclient.client.boot.setting.BasicExtensionSettings
import net.yakclient.client.util.toConfig
import java.nio.file.Path
import java.util.*

public class ExtensionLoader private constructor() {
    public companion object {
        private inline fun <reified T : Any> find(search: (T) -> Boolean = { true }): T =
            ServiceLoader.load(T::class.java).first(search)

        @JvmStatic
        public fun find(path: Path): ExtReference = find<Finder<ExtReference>>().find(path)

        @JvmStatic
        public fun resolve(ref: ExtReference, parent: Extension, dependencies: List<DependencyReference>): ClassLoader =
            find<Resolver<ExtReference>> { it.accepts.isAssignableFrom(ref::class.java) }.resolve(
                ref,
                parent,
                dependencies
            )

        @JvmStatic
        public fun load(ref: ExtReference, parent: Extension): Extension {
            val settings = ref.reader["ext-settings.conf"]?.asUri?.toConfig()?.extract<BasicExtensionSettings>("loader")
                ?: throw IllegalStateException("Failed to find or read ext-settings.conf file in module: ${ref.location.path}!")

            val repositories = settings.repositories?.map(YakClient.theGraph::ofRepository) ?: listOf()

            val references = settings.dependencies?.map { d ->
                repositories.firstNotNullOfOrNull { r -> r.load(d) }
                    ?: throw IllegalArgumentException("Failed to find dependency: $d")
            } ?: ArrayList()

            val loader: ClassLoader = resolve(ref, parent, references)

            val ext: Extension = loader.loadClass(settings.extensionClass).getConstructor().newInstance() as Extension

            ext.init(loader, settings, parent)

            return ext
        }
    }

    internal interface Finder<out T : ExtReference> {
        fun find(path: Path): T
    }

    internal interface Resolver<T : ExtReference> {
        val accepts: Class<T>

        fun resolve(ref: T, parent: Extension, dependencies: List<DependencyReference>): ClassLoader
    }
}

