package net.yakclient.client.ext.loader

import net.yakclient.client.api.ext.ExtSettings

public data class ResolvedExt(
    val settings: ExtSettings,
    val type: ExtensionType,
    val reference: ExtReference
)

public enum class ExtensionType {
    YAK,
    FORGE,
    FABRIC
}