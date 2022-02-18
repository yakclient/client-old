package net.yakclient.client.boot.loader

import java.net.URL

public interface ClComponent{
    public val packages : List<String>

    public fun loadClass(name: String) : Class<*>
}
