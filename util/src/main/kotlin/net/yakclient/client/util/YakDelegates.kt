package net.yakclient.client.util

import kotlin.reflect.KProperty

public fun <T : Any> immutableLateInit(): ImmutableLateInit<T> = ImmutableLateInit()

public class ImmutableLateInit<T : Any> {
    private var value: T? = null

    public operator fun getValue(thisRef: Any, property: KProperty<*>): T =
        value ?: throw IllegalStateException("Cannot query value at this time")

    public operator fun setValue(thisRef: Any, property: KProperty<*>, value: T): Unit = if (this.value == null) this.value =
        value else throw UnsupportedOperationException("Cannot set value at this time")
}

