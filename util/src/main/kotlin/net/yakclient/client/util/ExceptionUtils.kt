package net.yakclient.client.util

public inline fun <reified T> assertIs(it: Any, message: () -> String = { "Type ${it::class.qualifiedName} MUST implement ${T::class.qualifiedName}" }): T {
    assert(it is T) { message() }
    return it as T
}