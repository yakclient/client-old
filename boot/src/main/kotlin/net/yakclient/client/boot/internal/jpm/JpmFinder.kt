package net.yakclient.client.boot.internal.jpm

import net.yakclient.client.boot.archive.ArchiveFinder
import java.lang.module.ModuleFinder
import java.nio.file.Path

internal class JpmFinder : ArchiveFinder<JpmReference> {
    override fun find(path: Path): JpmReference =
        JpmReference(((ModuleFinder.of(path).findAll().takeUnless { it.size > 1 }
            ?: throw IllegalArgumentException("Cannot read more than 1 module at a time!")).firstOrNull()
            ?: throw IllegalArgumentException("Failed to find a readable module in: $path")))
}