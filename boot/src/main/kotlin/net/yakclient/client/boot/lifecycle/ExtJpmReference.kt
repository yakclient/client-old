package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.ext.ExtReference
import java.lang.module.Configuration
import java.lang.module.ModuleFinder
import java.util.stream.Collectors

internal class ExtJpmReference(
    internal val finder: ModuleFinder,
) : ExtReference(finder.findAll().map {
    it.open().let { r ->
        r.list().collect(Collectors.toMap({ name: String -> name }, { name: String -> r.find(name).get() }))
    }
}.fold(HashMap()) { acc, it -> acc.putAll(it); acc })
