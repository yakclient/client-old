package net.yakclient.client.internal.extension

public interface ExtensionTransformer : ExtLoadingProcess<ExtensionReference, ExtensionReference> {
    override val accepts: Class<ExtensionReference>
        get() = ExtensionReference::class.java
}