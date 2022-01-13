package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.ext.ExtensionLoader
import java.lang.module.Configuration
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference
import java.net.URI
import java.nio.file.Path

internal class ExtJpmReferencer : ExtensionLoader.Referencer<ExtJpmReference> {
    override fun reference(uris: List<URI>): ExtJpmReference {
        return ExtJpmReference(ModuleFinder.of(*uris.map(Path::of).toTypedArray()))
    }
}