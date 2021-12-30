package net.yakclient.client.internal.extension

public interface ExtensionLinker : ExtLoadingProcess<ExtensionReference, LinkedExtension> {
    override val accepts: Class<ExtensionReference>
        get() = ExtensionReference::class.java
}

public data class LinkedExtension(
    val classloader: ClassLoader
)