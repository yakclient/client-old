package net.yakclient.client.boot.loader

import net.yakclient.client.boot.archive.ResolvedArchive

public class ArchiveComponent(
    private val archive: ResolvedArchive
) : ClComponent {
    override val packages: Set<String> = archive.reference.reader.entries()
        .map { it.name } // Mapping to the name of the entry
        .filter { it.endsWith(".class") } // Ensuring only java classes
        .filterNot { it == "module-info.class" }
        .map { it.replace('/', '.').removeSuffix(".class") } // Normalizing names to appropriate class names
        .mapTo(HashSet()) { it.substring(0, it.lastIndexOf('.')) } // Mapping to a package names within a HashSet

    override fun loadClass(name: String): Class<*> = archive.classloader.loadClass(name)
}