package net.yakclient.client.util

public class LazyMap<K, out V>(
   private val delegate: MutableMap<K, V> = HashMap(),
    private val lazyImpl : (K) -> V?
) : Map<K, V> by delegate {
    override fun get(key: K): V? {
        if (delegate.contains(key)) return delegate[key]
        val lazyVal = lazyImpl(key)
        if (lazyVal != null) delegate[key] = lazyVal
        return lazyVal
    }
}