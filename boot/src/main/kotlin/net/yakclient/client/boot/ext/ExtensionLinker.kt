package net.yakclient.client.boot.ext

public interface ExtensionLinker : ExtLoadingProcess<ExtensionReference, LinkedExtension>

public interface LinkedExtension {
    public val classloader: ClassLoader
}