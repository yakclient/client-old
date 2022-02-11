package net.yakclient.client.boot.internal.jpm

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.archive.ArchiveResolver
import net.yakclient.client.boot.archive.ResolvedArchive
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
import kotlin.reflect.KClass

private const val TEMP_JAR_SUFFIX = "YAK-ALTERED"

public class JpmResolver : ArchiveResolver<JpmReference> {
    override val accepts: KClass<JpmReference> = JpmReference::class

    override fun resolve(ref: JpmReference, parents: List<ResolvedArchive>): ResolvedArchive {
        val finder = loadFinder(ref)
//        val finder = ModuleFinder.of(dep)
//        assert(finder.findAll().size == 1) { "Only able to load one dependency at a time!" }
//        val reference = finder.findAll().first()

        assert(
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

        val references = parents.filterIsInstance<ResolvedJpm>().takeIf { it.size == parents.size }
            ?: throw IllegalArgumentException("All parents must be of type ${ResolvedJpm::class.simpleName}")

        val configuration = Configuration.resolve(
            finder,
            references.map(ResolvedJpm::configuration) + ModuleLayer.boot().configuration(),
            ModuleFinder.of(),
            listOf(ref.name)
        )

        // val loader = JpmLoader(YakClient.loader, ref)
        //
        //        val controller = ModuleLayer.defineModules(
        //            configuration,
        //            references.map(ResolvedJpm::layer) + ModuleLayer.boot()
        //        ) { loader }
        val controller = ModuleLayer.defineModulesWithOneLoader(
            configuration,
            references.map(ResolvedJpm::layer) + ModuleLayer.boot(),
            YakClient.loader
        )

        val layer = controller.layer()

        layer.modules().forEach { m ->
            m.packages.forEach { p ->
                controller.addExports(m, p, YakClient::class.java.module)
            }
        }

        return ResolvedJpm(layer.modules().first())
    }

    private fun loadFinder(ref: JpmReference): ModuleFinder = if (!ref.modified) ProvidedModuleFinder(ref) else {
        val temp = YakClient.settings.moduleTempPath
        val desc = ref.descriptor()
        val jar = temp resolve "${desc.name().replace('.', '-')}${desc.rawVersion().map { "-$it-$TEMP_JAR_SUFFIX" }.orElse("")}.jar"

        Files.deleteIfExists(jar)
        jar.make()


       JarOutputStream(FileOutputStream(jar.toFile())).use { target ->
            ref.reader.entries().forEach { e ->
                val entry = JarEntry(e.name)

                target.putNextEntry(entry)

                val eIn = e.asInputStream
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

        ModuleFinder.of(jar)
    }
}

private class ProvidedModuleFinder(
    private val ref: JpmReference
) : ModuleFinder {
    override fun find(name: String): Optional<ModuleReference> =
        if (ref.descriptor().name() == name) Optional.of(ref) else Optional.empty()

    override fun findAll(): MutableSet<ModuleReference> = mutableSetOf(ref)
}