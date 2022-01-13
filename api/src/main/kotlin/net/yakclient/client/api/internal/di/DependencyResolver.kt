package net.yakclient.client.api.internal.di

public class DependencyResolver internal constructor(
    private val context: DIContext
) {
    private val extras: MutableList<Any> = ArrayList()

    public fun supply(any: Any): Unit = let { extras.add(any) }

    public fun <T: Any> resolve(type: Class<T>) : T? = (extras.find { type::class.java.isAssignableFrom(it::class.java) } ?: context.resolve(type)) as? T
}