package net.yakclient.client.api.restricted.di

public class DIContext(
    private val providers: List<InjectionProvider<*>>
) {
    public fun <T: Any> resolve(type: Class<T>) : T? = (providers.find { type::class.java.isAssignableFrom(it::class.java) } as? InjectionProvider<T>)?.get()
}