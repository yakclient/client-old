package net.yakclient.client.internal.lifecycle

import net.yakclient.client.internal.extension.*
import java.io.File
import java.io.InputStream
import java.net.URI
import java.nio.file.Paths
import java.util.jar.JarFile

public class JarModuleReferencer : ExtensionReferencer {
    override fun process(toProcess: URI): ExtensionReference = ExtensionReference(buildMap {
        val jar = JarFile(Paths.get(toProcess).toFile())

        for (entry in jar.entries()) {
            put(entry.name, object : ExtensionEntry {
                override val name: String = entry.name

                override fun asInputStream(): InputStream = jar.getInputStream(entry)
            })
        }
    })
}

public fun ExtensionLoader.loadJar(file: File): Extension? = load(JarModuleReferencer().process(file.toURI()))