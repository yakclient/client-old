package net.yakclient.client.boot.ext

public fun interface ExtLoadingProcess<in I : Any, out O : Any> {
    public fun process(toProcess: I) : O
}

