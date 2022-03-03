package net.yakclient.client.boot.extension

import io.github.config4k.extract
import net.yakclient.client.boot.dependency.DependencyGraph
import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.boot.loader.ArchiveComponent
import net.yakclient.client.boot.loader.ArchiveLoader
import net.yakclient.client.boot.setting.BasicExtensionSettings
import net.yakclient.client.boot.setting.ExtensionSettings
import net.yakclient.client.util.toConfig
import java.util.*

public class ExtensionLoader private constructor() {
    public companion object {
        @JvmStatic
        public fun loadDependencies(settings: ExtensionSettings): List<ResolvedArchive> {
            val repositories = settings.repositories?.map(DependencyGraph::ofRepository) ?: listOf()

            return settings.dependencies?.map { d ->
                repositories.firstNotNullOfOrNull { r -> r.load(d) }
                    ?: throw IllegalArgumentException("Failed to find dependency: $d")
            } ?: ArrayList()
        }

        @JvmStatic
        public fun loadSettings(ref: ArchiveReference): BasicExtensionSettings =
            ref.reader["ext-settings.conf"]?.asUri?.toConfig()?.extract<BasicExtensionSettings>("loader")
                ?: throw IllegalStateException("Failed to find or read ext-settings.conf file in module: ${ref.location.path}!")

        @JvmStatic
        @JvmOverloads
        public fun load(
            ref: ArchiveReference,
            parent: Extension,
            settings: ExtensionSettings = loadSettings(ref),
            dependencies: List<ResolvedArchive> = loadDependencies(settings)
        ): Extension {
            val loader = ArchiveLoader(parent.ref.classloader, dependencies.map(::ArchiveComponent), ref)

            val archive: ResolvedArchive = ArchiveUtils.resolve(ref, loader, dependencies + parent.ref)

            val ext: Extension =
                archive.classloader.loadClass(settings.extensionClass).getConstructor().newInstance() as Extension

            ext.init(archive, settings, parent)

            return ext
        }
    }
}

