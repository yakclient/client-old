package net.yakclient.client.boot.ext

import io.github.config4k.extract
import net.yakclient.client.boot.lifecycle.BasicExtensionSettings
import net.yakclient.client.boot.lifecycle.ClassDefiner
import net.yakclient.client.util.assertIs
import net.yakclient.client.util.openStream
import net.yakclient.client.util.readInputStream
import net.yakclient.client.util.toConfig
import java.net.URI
import java.util.*

public class ExtensionLoader private constructor() {
    public companion object {
        private inline fun <reified T : Any> find(search: (T) -> Boolean = { true }): T =
            ServiceLoader.load(T::class.java).first(search)

        @JvmStatic
        public fun reference(vararg uris: URI): ExtReference = reference(uris.toList())

        @JvmStatic
        public fun reference(uris: List<URI>): ExtReference = find<Referencer<ExtReference>>().reference(uris)

        @JvmStatic
        public fun load(ref: ExtReference, parent: Extension): Extension {
            val resolver = find<Resolver<ExtReference>> { it.accepts.isAssignableFrom(ref::class.java) }
            val settings = (ref["ext-settings.conf"]?.toConfig()?.extract<BasicExtensionSettings>("loader")
                ?: throw IllegalStateException("Extension must have ext-settings.conf file!"))

            val (main) = settings

            val loader: ClassLoader = resolver.resolve(ref, parent)

//                ref[sl]?.let {
//                val loader = (assertIs<ClassDefiner>(parent.loader).defineClass(sl!!, readInputStream(it.openStream())))
//                assert(ClassDefiner::class.java.isAssignableFrom(loader))
//                loader.getConstructor(ExtReference::class.java).newInstance(ref) as ClassLoader
//            } ?: resolver.resolve(ref, parent.loader)

            assert(ref.contains(main)) { "Failed to find extension class $main, make sure the definition in your ext-settings.conf and extension match!" }

            val ext: Extension = (loader.loadClass(main)).getConstructor().newInstance() as Extension

            ext.init(loader, settings,  parent)

            return ext
        }
    }

    internal interface Referencer<out T : ExtReference> {
        fun reference(uris: List<URI>): T
    }

    internal interface Resolver<T : ExtReference> {
        val accepts: Class<T>

        fun resolve(ref: T, parent: Extension): ClassLoader
    }
}

//public sealed class ExtensionLoader(
//    private val processes: List<ExtPipeStage>
//) {
//    public fun load(ref: ExtensionReference): Extension =
//        processes.fold<ExtPipeStage, Any>(ref) { acc, it ->
//            it::process.call(acc)
//        } as Extension
//}
//
//public class MutableExtLoader private constructor(
//    private val backing_list: MutableList<ExtPipeStage>
//) {
//    public constructor() : this(ArrayList())
//
//    public fun <N : Any> add(process: ExtLoadingProcess<ExtensionReference, N>): MutableLoadingProcess<N> =
//        addInternal(process)
//
//    private fun <I: Any, N : Any> addInternal(process: ExtLoadingProcess<I, N>): MutableLoadingProcess<N> =
//        MutableLoadingProcess<N>().also { backing_list.add(process) }
//
//    public inner class MutableLoadingProcess<T : Any> internal constructor() {
//        public fun <N : Any> add(process: ExtLoadingProcess<T, N>): MutableLoadingProcess<N> = addInternal(process)
//
//        public fun toLoader(): ExtensionLoader = ExtensionLoaderImpl()
//    }
//
//    private inner class ExtensionLoaderImpl : ExtensionLoader(backing_list)
//}

