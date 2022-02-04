package net.yakclient.client.boot.internal

import net.yakclient.client.boot.ext.ExtensionLoader
import java.lang.module.ModuleFinder
import java.nio.file.Path

internal class ExtJpmFinder : ExtensionLoader.Finder<JpmReference> {
    override fun find(path: Path): JpmReference =
        JpmReference(((ModuleFinder.of(path).findAll().takeUnless { it.size > 1 }
            ?: throw IllegalArgumentException("Cannot read more than 1 module at a time!")).firstOrNull()
            ?: throw IllegalArgumentException("Failed to find a readable module in: $path")))
}