package net.yakclient.client.internal.extension

// TODO @JvmDefaultWithoutCompatibility
public interface Extension {
    public val parent: Extension?
    public val loader: ClassLoader

    public fun onLoad() {}
}