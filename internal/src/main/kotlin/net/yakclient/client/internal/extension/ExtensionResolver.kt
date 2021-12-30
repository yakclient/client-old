package net.yakclient.client.internal.extension

public interface ExtensionResolver : ExtLoadingProcess<LinkedExtension, Extension> {
    override val accepts: Class<LinkedExtension>
        get() = LinkedExtension::class.java
}