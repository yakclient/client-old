package net.yakclient.client.util

public fun <T> T.equalsAny(vararg all: T): Boolean = all.any { this == it }

public fun <T> T.equalsAll(vararg all: T): Boolean = all.all { this == it }

public fun <T, K> Collection<T>.filterDuplicatesBy(check: (T) -> K): List<T> {
    val toMatch = HashSet<K>()

    return filter { toMatch.add(check(it)) }
}

public fun <T> Collection<T>.filterDuplicates() : List<T> = filterDuplicatesBy { it }