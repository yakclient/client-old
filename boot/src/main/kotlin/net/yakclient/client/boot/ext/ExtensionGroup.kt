package net.yakclient.client.boot.ext

public interface ExtensionGroup {
    public val parent: ExtensionGroup?

    public val extensions: List<Extension>

    public fun define()

    public fun resolve()
}