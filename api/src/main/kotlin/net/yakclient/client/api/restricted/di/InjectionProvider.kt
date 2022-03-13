package net.yakclient.client.api.restricted.di

import java.util.function.Supplier

public interface InjectionProvider<T: Any> : Supplier<T> {
    public val provides: Class<T>
}