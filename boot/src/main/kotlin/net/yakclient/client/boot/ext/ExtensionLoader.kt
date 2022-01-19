package net.yakclient.client.boot.ext

import io.github.config4k.extract
import net.yakclient.client.boot.setting.BasicExtensionSettings
import net.yakclient.client.util.toConfig
import java.net.URI
import java.util.*

public class ExtensionLoader private constructor() {
    public companion object {
        private inline fun <reified T : Any> find(search: (T) -> Boolean = { true }): T =
            ServiceLoader.load(T::class.java).first(search)

        @JvmStatic
        public fun find(uri: URI): ExtReference = find<Finder<ExtReference>>().find(uri)

        @JvmStatic
        public fun resolve(ref: ExtReference, parent: Extension) : ClassLoader = find<Resolver<ExtReference>> { it.accepts.isAssignableFrom(ref::class.java) }.resolve(ref, parent)

        @JvmStatic
        public fun load(ref: ExtReference, parent: Extension): Extension {
            val settings = ref.reader["ext-settings.conf"]?.asUri?.toConfig()?.extract<BasicExtensionSettings>("loader")
                ?: throw IllegalStateException("Failed to find or read ext-settings.conf file in module: ${ref.location.path}!")

            val (main) = settings

            val loader: ClassLoader = resolve(ref, parent)

            val ext: Extension = (loader.loadClass(main)).getConstructor().newInstance() as Extension

            ext.init(loader, settings, parent)

            return ext
        }
    }

    internal interface Finder<out T : ExtReference> {
        fun find(uri: URI): T
    }

    internal interface Resolver<T : ExtReference> {
        val accepts: Class<T>

        fun resolve(ref: T, parent: Extension): ClassLoader
    }
}

