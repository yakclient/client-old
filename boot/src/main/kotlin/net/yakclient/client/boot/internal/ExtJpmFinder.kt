package net.yakclient.client.boot.internal

import net.yakclient.client.boot.ext.ExtensionLoader
import java.lang.module.ModuleFinder
import java.net.URI
import java.nio.file.Path

internal class ExtJpmFinder : ExtensionLoader.Finder<JpmReference> {
    override fun find(uri: URI): JpmReference =
        JpmReference(((ModuleFinder.of(Path.of(uri)).findAll().takeUnless { it.size > 1 }
            ?: throw IllegalArgumentException("Cannot read more than 1 module at a time!")).firstOrNull()
            ?: throw IllegalArgumentException("Failed to find a readable module in: ${uri.path}")))
}