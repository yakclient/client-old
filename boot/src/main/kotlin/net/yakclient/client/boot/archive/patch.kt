package net.yakclient.client.boot.archive

public fun Collection<ArchiveReference>.patch(vararg toPatch: Set<String>): List<ArchiveReference> {
    val setOf = toPatch.flatMapTo(HashSet()) { it }

    val ignored = filter { !setOf.contains(it.name) }

    val patched: List<ArchiveReference> = toPatch.map { current ->
        check(current.size >= 2) { "Must be atleast 2 archives to patch!" }

        val patches = filterTo(ArrayList()) { current.contains(it.name) }

        patches.fold(patches.first()) { acc, it -> acc.and(it) }
    }

    return ignored + patched
}

