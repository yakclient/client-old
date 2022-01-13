package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.ext.*
import java.io.File
import java.io.InputStream
import java.net.JarURLConnection
import java.net.URI
import java.net.URL
import java.nio.file.Paths
import java.util.jar.JarFile

public fun loadJar(uri: URI): ExtReference = JarReference(
    JarFile(Paths.get(uri).toFile()).entries().toList()
        .associate { it.name to URI.create("jar:file:${File.separator}${uri.path.removePrefix("/")}!/${it.name}") })

public class JarReference(delegate: Map<String, URI>) : ExtReference(delegate)

//public fun ExtensionLoader.loadJar(uri: URI): Extension =
//    load(
//        net.yakclient.client.boot.lifecycle.loadJar(uri)
//            .also { check((it.isNotEmpty())) { "Failed to load jar entries for uri: $uri" } })