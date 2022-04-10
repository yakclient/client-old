package net.yakclient.client.util

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

public suspend fun <T, K> Collection<T>.mapNotBlocking(transformer: suspend (T) -> K): List<K> = coroutineScope {
    val jobs = map { v -> async { transformer(v) } }

    jobs.map { it.await() }
}

public fun <T, K> Collection<T>.mapBlocking(transformer: suspend (T) -> K): List<K> = runBlocking { this@mapBlocking.mapNotBlocking(transformer) }