package net.yakclient.client.util

public fun ClassLoader.loadClassOrNull(name: String) : Class<*>? = runCatching { loadClass(name) }.getOrNull()