package net.yakclient.client.util

import kotlinx.coroutines.*

private suspend fun <T, K> Iterable<T>.mapJobs(transformer: suspend (T) -> K): List<Deferred<K>> =
    coroutineScope { map { v -> async { transformer(v) } } }


public suspend fun <T, K> Iterable<T>.mapNotBlocking(transformer: suspend (T) -> K): List<K> =
    mapJobs(transformer).map { it.await() }

public fun <T, K> Iterable<T>.mapBlocking(transformer: suspend (T) -> K): List<K> =
    runBlocking { this@mapBlocking.mapNotBlocking(transformer) }

public fun <T, K> Iterable<T>.mapNotNullBlocking(transformer: suspend (T) -> K?): List<K> =
    runBlocking { mapJobs(transformer).mapNotNull { it.await() } }

public suspend fun <T> Iterable<T>.forEachNotBlocking(action: suspend (T) -> Unit): Unit = coroutineScope {
    forEach { launch { action(it) } }
}

public fun <T> Iterable<T>.forEachBlocking(action: suspend (T) -> Unit) : Unit = runBlocking { forEachNotBlocking(action) }
