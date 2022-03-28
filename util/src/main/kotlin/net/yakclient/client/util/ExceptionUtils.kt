package net.yakclient.client.util

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

public inline fun <T> runCatching(exception: KClass<out Exception>, crossinline block: () -> T): T? = runCatching(block).let {
    it.getOrNull()
        ?: if (it.exceptionOrNull()!!::class.isSubclassOf(exception)) null else throw it.exceptionOrNull()!!
}
