package net.yakclient.client.internal.extension

public interface ExtLoadingProcess<I, out O> {
    public val accepts: Class<I>

    public fun process(toProcess: I) : O?
}

