package net.yakclient.client.internal.extension

public typealias ModulePipeStage = ExtLoadingProcess<out Any, Any>

public class ExtensionLoader(
    private val processes: List<ModulePipeStage>
) {
    public fun load(ref: ExtensionReference): Extension? =
        processes.fold<ModulePipeStage, Any?>(ref) { nacc, it ->
            nacc?.let { acc ->
                if (it.accepts.isAssignableFrom(
                        acc::class.java
                    )
                ) (it as ExtLoadingProcess<Any, Any>).process(acc) else null
            }
        } as? Extension
}