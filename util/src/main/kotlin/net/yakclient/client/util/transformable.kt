package net.yakclient.client.util

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

public fun <T, I, O> transformable(delegate: KProperty1<T, I>,mapper: (I) -> O): ReadOnlyProperty<T, O> = ReadOnlyProperty { thisRef, _ ->
    mapper(delegate.get(thisRef))
}

public fun <T, I, O> transformable(delegate: KProperty0<I>,mapper: (I) -> O): ReadOnlyProperty<T, O> = ReadOnlyProperty { _, _ ->
    mapper(delegate.get())
}