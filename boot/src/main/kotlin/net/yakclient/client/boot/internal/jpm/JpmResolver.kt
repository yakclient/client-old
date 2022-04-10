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
import kotlin.reflect.KClass

internal class JpmResolver : ArchiveResolver<JpmReference> {
    override val type: KClass<JpmReference> = JpmReference::class

    override fun resolve(
        archiveRefs: List<JpmReference>,
        clProvider: ClassLoaderProvider<JpmReference>,
        parents: List<ResolvedArchive>
    ): List<ResolvedArchive> {
        val refs = archiveRefs.map(::loadRef)
        val refsByName = refs.associateBy { it.descriptor().name() }

//        val archivesByName: Map<String, JpmReference> = archiveRefs.associateBy { it.descriptor().name() }

        for (ref in refs) assert(
            ref.descriptor().requires()
                .filterNot { it.modifiers().contains(ModuleDescriptor.Requires.Modifier.STATIC) }
                .all { r ->
                    fun Configuration.provides(name: String): Boolean =
                        modules().any { it.name() == name } || parents().any { it.provides(name) }
//
//                    fun ResolvedArchive.provides(name: String): Boolean = if (this is ResolvedJpmArchive) {
//                        module.name == name || this.configuration.provides(name)
//                    } else {
//                        false
//                    }
//                        ref.descriptor().provides().any { it.name() == name } || parents.any { it.provides(name) }

                    parents.filterIsInstance<ResolvedJpmArchive>().any {
                        it.module.name == r.name() || it.configuration.provides(r.name())
                    } || ModuleLayer.boot().configuration().provides(r.name()) || refs.any {
                        it.descriptor().name() == r.name()
                    }

//                    parents.any { d -> r.name() == d.name || (d as ResolvedJpmArchive).configuration.provides(r.name()) }
//                            || ModuleLayer.boot().modules().any { d -> r.name() == d.name }
//                            || refs.any { d -> d.descriptor().name() == r.name() }
                }) {
            "A Dependency of ${ref.descriptor().name()} is not in the graph!"
        }

        val loaders = LazyMap<String, ClassLoader> {
            clProvider(
                refsByName[it] ?: throw IllegalStateException(
                    "Error occurred when trying to create a class loader for module $it because module $it is not recognized. Only suppose to load modules for ${
                        refs.joinToString(
                            prefix = "[",
                            postfix = "]"
                        ) { m -> m.descriptor().name() }
                    }"
                ),
            )
        }

        // Mapping to a HashSet to avoid multiple configuration that are the same(they do not override equals and hashcode but using the object ID's should be good enough)
        val parentLayers =
            parents.filterIsInstance<ResolvedJpmArchive>().mapTo(HashSet()) { it.layer }

        val finder = object : ModuleFinder {
            override fun find(name: String): Optional<ModuleReference> = Optional.ofNullable(refsByName[name])

            override fun findAll(): MutableSet<ModuleReference> = refsByName.values.toMutableSet()//.toMutableSet()
        }

        val configuration = Configuration.resolveAndBind(
            finder,
            parentLayers.map { it.configuration() } + ModuleLayer.boot().configuration(),
            ModuleFinder.of(),
            finder.findAll().map(ModuleReference::descriptor).map(ModuleDescriptor::name)
        )


        val controller = ModuleLayer.defineModules(
            configuration,
            parentLayers.toList() + ModuleLayer.boot(),
            loaders::get
        )

        val layer = controller.layer()

        layer.modules().forEach { m ->
            m.packages.forEach { p ->
                controller.addOpens(m, p, YakClient::class.java.module)
            }
        }

        return layer.modules()
            .map { m ->
                ResolvedJpmArchive(
                    m,
                    archiveRefs.first { it.descriptor().name() == m.name })
            }//.map(::ResolvedJpm)
    }

    private fun loadRef(ref: JpmReference): JpmReference = if (!ref.modified) ref else {
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
//
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

        JpmReference(ModuleFinder.of(jar).find(ref.descriptor().name())
            .orElseThrow { IllegalArgumentException("Archive reference that should be present is not! Path: $jar") })
    }
}