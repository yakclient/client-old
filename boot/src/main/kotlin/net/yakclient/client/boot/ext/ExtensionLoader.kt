package net.yakclient.client.boot.ext

public typealias ExtPipeStage = ExtLoadingProcess<*, *>

public sealed class ExtensionLoader(
    private val processes: List<ExtPipeStage>
) {
    public fun load(ref: ExtensionReference): Extension =
        processes.fold<ExtPipeStage, Any>(ref) { acc, it ->
            it::process.call(acc)
        } as Extension
}

public class MutableExtLoader private constructor(
    private val backing_list: MutableList<ExtPipeStage>
) {
    public constructor() : this(ArrayList())

    public fun <N : Any> add(process: ExtLoadingProcess<ExtensionReference, N>): MutableLoadingProcess<N> =
        addInternal(process)

    private fun <I: Any, N : Any> addInternal(process: ExtLoadingProcess<I, N>): MutableLoadingProcess<N> =
        MutableLoadingProcess<N>().also { backing_list.add(process) }

    public inner class MutableLoadingProcess<T : Any> internal constructor() {
        public fun <N : Any> add(process: ExtLoadingProcess<T, N>): MutableLoadingProcess<N> = addInternal(process)

        public fun toLoader(): ExtensionLoader = ExtensionLoaderImpl()
    }

    private inner class ExtensionLoaderImpl : ExtensionLoader(backing_list)
}

