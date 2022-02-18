package net.yakclient.client.boot.internal.jpm

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.archive.ArchiveResolver
import net.yakclient.client.boot.archive.ClassLoaderProvider
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.util.LazyMap
import net.yakclient.client.util.make
import net.yakclient.client.util.resolve
import java.io.FileOutputStream
import java.lang.module.Configuration
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference
import java.nio.file.Files
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.collections.HashSet

internal class JpmResolver : ArchiveResolver<JpmReference> {
    override fun resolve(
        refs: List<JpmReference>,
        clProvider: ClassLoaderProvider<JpmReference>,
        parents: List<ResolvedArchive>
    ): List<ResolvedArchive> {
        val modulesByName = refs.associateBy { it.descriptor().name() }

        val finder = object : ModuleFinder {
            override fun find(name: String): Optional<ModuleReference> = Optional.ofNullable(modulesByName[name])

            override fun findAll(): MutableSet<ModuleReference> = refs.toMutableSet()
        }
        for (ref in refs) assert(
            ref.descriptor().requires()
                .filterNot { it.modifiers().contains(ModuleDescriptor.Requires.Modifier.STATIC) }
                .all { r ->
                    fun Configuration.provides(name: String): Boolean =
                        modules().any { it.name() == name } || parents().any { it.provides(name) }

                    parents.any { d -> r.name() == d.name || (d as ResolvedJpm).configuration.provides(r.name()) } || ModuleLayer.boot()
                        .modules().any { d -> r.name() == d.name }
                }) {
            "A Dependency of ${ref.descriptor().name()} is not in the graph!"
        }

        // Mapping to a HashSet to avoid multiple configuration that are the same(they do not override equals and hashcode but using the object ID's should be good enough)
        val parentLayers =
            parents.filterIsInstance<ResolvedJpm>().mapTo(HashSet()) { it.layer }

        val configuration = Configuration.resolve(
            finder,
            parentLayers.map { it.configuration() } + ModuleLayer.boot().configuration(),
            ModuleFinder.of(),
            finder.findAll().map(ModuleReference::descriptor).map(ModuleDescriptor::name)
        )

        val loaders = LazyMap<String, ClassLoader> {
            clProvider(
                modulesByName[it] ?: throw IllegalStateException(
                    "Error occurred when trying to create a class loader for module $it because module $it is not recognized. Only suppose to load modules for ${
                        refs.joinToString(
                            prefix = "[",
                            postfix = "]",
                            transform = ArchiveReference::name
                        )
                    }"
                ),
            )
//            JpmLoader(
//                parent,
//                modulesByName[it] ?: throw IllegalStateException(
//                    "Error occurred when trying to create a class loader for module $it because module $it is not recognized. Only suppose to load modules for ${
//                        refs.joinToString(
//                            prefix = "[",
//                            postfix = "]",
//                            transform = ArchiveReference::name
//                        )
//                    }"
//                ),
//                parents.filterNot { c -> c.name == it } as List<ResolvedJpm>
//            )
        }

        val controller = ModuleLayer.defineModules(
            configuration,
            parentLayers.toList() + ModuleLayer.boot(),
            loaders::get
        )

        val layer = controller.layer()

        layer.modules().forEach { m ->
            m.packages.forEach { p ->
                controller.addExports(m, p, YakClient::class.java.module)
            }
        }

        return layer.modules().map(::ResolvedJpm)
    }

//    private fun loadFinder(refs: List<JpmReference>): ModuleFinder = ProvidedModuleFinder(refs.map(::loadRef))

    private fun loadRef(ref: JpmReference): ModuleReference = if (!ref.modified) ref else {
        val temp = YakClient.settings.moduleTempPath
        val desc = ref.descriptor()
        val jar = temp resolve "${desc.name().replace('.', '-')}${
            desc.rawVersion().map { "-$it" }.orElse("")
        }.jar"

        Files.deleteIfExists(jar)
        jar.make()


        JarOutputStream(FileOutputStream(jar.toFile())).use { target ->
            ref.reader.entries().forEach { e ->
                val entry = JarEntry(e.name)

                target.putNextEntry(entry)

                val eIn = e.asInputStream

                //Stolen from https://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
                val buffer = ByteArray(1024)

                while (true) {
                    val count: Int = eIn.read(buffer)
                    if (count == -1) break

                    target.write(buffer, 0, count)
                }

                target.closeEntry()
            }

        }

        assert(Files.exists(jar)) { "Failed to write jar to temp directory!" }

        ModuleFinder.of(jar).find(ref.name)
            .orElseThrow { IllegalArgumentException("Archive reference that should be present is not! Path: $jar") }
    }


}


private class ProvidedModuleFinder(
    _refs: List<ModuleReference>
) : ModuleFinder {
    private val refs: Map<String, ModuleReference> = _refs.associateBy { it.descriptor().name() }

    override fun find(name: String): Optional<ModuleReference> {
        val ref = refs[name]
        return if (ref?.descriptor()?.name() == name) Optional.of(ref)
        else Optional.empty()
    }

    override fun findAll(): MutableSet<ModuleReference> = refs.values.toMutableSet()
}