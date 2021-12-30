package net.yakclient.client.ext.loader

public interface LoadingProcess<I, out O> {
    public val accepts: Class<I>

    public fun process(toProcess: I) : O?
}