package net.yakclient.client.boot.extension

import io.github.config4k.extract
import net.yakclient.archives.ArchiveHandle
import net.yakclient.archives.Archives
import net.yakclient.archives.ResolvedArchive
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.dependency.DependencyGraph
import net.yakclient.client.boot.loader.ArchiveComponent
import net.yakclient.client.boot.loader.ArchiveLoader
import net.yakclient.client.boot.maven.MAVEN_LOCAL
import net.yakclient.client.util.toConfig
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger

public object ExtensionLoader {
    private val logger = Logger.getLogger(this::class.simpleName)

    @JvmStatic
    public fun loadDependencies(settings: ExtensionSettings): Set<ResolvedArchive> {
        val repositories =
            settings.repositories?.map { DependencyGraph.ofRepository(it, YakClient.dependencyResolver) } ?: listOf()

        return settings.dependencies?.flatMapTo(HashSet()) { d ->
            repositories.firstNotNullOfOrNull { r -> r.load(d).takeIf(Collection<*>::isNotEmpty) }
                ?: throw IllegalArgumentException("Extension '${settings.name}' has a required dependency of '$d' which cannot be found in the specified repositories: '${
                    settings.repositories?.joinToString(prefix = "[", postfix = "]") {
                        "{type=${it.type}, options: ${
                            it.options.map { (k, v) -> "'$k'='$v'" }.joinToString(prefix = "[", postfix = "]")
                        }"
                    } ?: "[]"
                }'")
        } ?: HashSet()
    }

    @JvmStatic
    public fun loadSettings(ref: ArchiveHandle): BasicExtensionSettings =
        ref.reader["ext-settings.conf"]?.resource?.uri?.toConfig()?.extract<BasicExtensionSettings>("loader")
            ?: throw IllegalStateException("Failed to find or read ext-settings.conf file in module: ${ref.location.path}!")

    @JvmStatic
    public fun load(
        path: Path,
        parent: Extension,
    ): Extension {
        val ref = Archives.find(path, Archives.Finders.JPM_FINDER)
        val settings = loadSettings(ref)
        return load(ref, parent, settings, loadDependencies(settings))
    }

    @JvmStatic
    @JvmOverloads
    public fun load(
        ref: ArchiveHandle,
        parent: Extension,
        settings: ExtensionSettings = loadSettings(ref),
        dependencies: Set<ResolvedArchive> = loadDependencies(settings),
        loader: ClassLoader = ArchiveLoader(parent.ref.classloader, dependencies.map(::ArchiveComponent), ref)
    ): Extension {
        if (settings.repositories?.any { it.type == MAVEN_LOCAL } == true) logger.log(
            Level.WARNING,
            "Extension: '${settings.name}' contains a repository referencing maven local! Make sure this is removed in all production builds."
        )

        val (archive, controller, module) = Archives.resolve(
            ref,
            loader,
            Archives.Resolvers.JPM_RESOLVER,
            (dependencies + parent.ref).toHashSet()
        )

        archive.packages.forEach { p ->
            controller.addOpens(module, p, YakClient::class.java.module)
        }

        val ext: Extension =
            archive.classloader.loadClass(settings.extensionClass).getConstructor().newInstance() as Extension

        ext.init(archive, settings, parent)

        return ext
    }
}

