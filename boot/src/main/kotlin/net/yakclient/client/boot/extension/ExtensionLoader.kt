package net.yakclient.client.boot.extension

import io.github.config4k.extract
import net.yakclient.client.boot.dependency.DependencyGraph
import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.boot.loader.ArchiveComponent
import net.yakclient.client.boot.loader.ArchiveLoader
import net.yakclient.client.boot.repository.RepositoryType
import net.yakclient.client.boot.setting.BasicExtensionSettings
import net.yakclient.client.boot.setting.ExtensionSettings
import net.yakclient.client.util.toConfig
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

public object ExtensionLoader {
    private val logger = Logger.getLogger(this::class.simpleName)

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
        if (settings.repositories?.any { it.type == RepositoryType.MAVEN_LOCAL } == true) logger.log(
            Level.WARNING,
            "Extension: '${settings.name}' contains a repository referencing maven local! Make sure this is removed in all production builds."
        )

        val loader = ArchiveLoader(parent.ref.classloader, dependencies.map(::ArchiveComponent), ref)

        val archive: ResolvedArchive = ArchiveUtils.resolve(ref, loader, dependencies + parent.ref)

        val ext: Extension =
            archive.classloader.loadClass(settings.extensionClass).getConstructor().newInstance() as Extension

        ext.init(archive, settings, parent)

        return ext
    }
}

