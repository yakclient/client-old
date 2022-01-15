package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.ext.ExtensionLoader
import java.lang.module.ModuleFinder
import java.net.URI
import java.nio.file.Path
import java.util.jar.JarFile

internal class ExtJpmReferencer : ExtensionLoader.Referencer<JpmReference> {
    override fun reference(uri: URI): JpmReference {
        TODO("")
//        return JpmReference(ModuleFinder.of(*uris.map(Path::of).toTypedArray()))
    }
}