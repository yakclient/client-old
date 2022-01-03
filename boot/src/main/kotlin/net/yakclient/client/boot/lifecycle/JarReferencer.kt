package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.ext.*
import java.io.File
import java.io.InputStream
import java.net.JarURLConnection
import java.net.URI
import java.net.URL
import java.nio.file.Paths
import java.util.jar.JarFile

public class JarReferencer : ExtensionReferencer {
    override fun process(toProcess: URI): ExtensionReference = loadJar(toProcess)
}

public fun loadJar(uri: URI) : ExtensionReference = ExtensionReference(buildMap {
    val jar = JarFile(Paths.get(uri).toFile())


    for (entry in jar.entries()) {
        put(entry.name, object : ExtensionEntry {
            override val name: String = entry.name

            //TODO Seems very operating system dependenant, figure this out
            override fun asURI(): URI {
                return URI.create("jar:file:${File.separator}${uri.path.removePrefix("/")}!/${entry.name}")
            }

            override fun asInputStream(): InputStream = jar.getInputStream(entry)
        })// NamedExtensionEntry(entry.name) {jar.getInputStream(entry)})
    }
})

public fun ExtensionLoader.loadJar(uri: URI): Extension =
    load(JarReferencer().process(uri).also { check((it.isNotEmpty())) { "Failed to load jar entries for uri: $uri" } })