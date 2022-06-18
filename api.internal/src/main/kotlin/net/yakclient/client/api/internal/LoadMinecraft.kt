package net.yakclient.client.api.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import net.yakclient.archives.Archives
import net.yakclient.archives.ResolvedArchive
import net.yakclient.archives.jpm.JpmResolutionResult
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.dependency.DependencyCache
import net.yakclient.client.boot.dependency.DependencyGraph
import net.yakclient.client.boot.dependency.orFallBackOn
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.loader.ArchiveComponent
import net.yakclient.client.boot.loader.ArchiveSourceProvider
import net.yakclient.client.boot.loader.ClConglomerate
import net.yakclient.client.boot.loader.SourceProvider
import net.yakclient.client.boot.maven.MavenDescriptor
import net.yakclient.common.util.copyTo
import net.yakclient.common.util.make
import net.yakclient.common.util.mapNotNullBlocking
import net.yakclient.common.util.resolve
import net.yakclient.common.util.resource.SafeResource
import java.net.URL
import java.nio.ByteBuffer
import java.nio.file.Path
import java.util.logging.Level

private infix fun SafeResource.copyToBlocking(to: Path): Path = runBlocking { this@copyToBlocking copyTo to }

internal fun Extension.loadMinecraft(): ResolvedArchive {
    // Convert an operating system name to its type
    fun String.osNameToType(): OsType? = when (this) {
        "linux" -> OsType.UNIX
        "windows" -> OsType.WINDOWS
        "osx" -> OsType.OS_X
        else -> null
    }

    // Convert an operating system type to its name
    fun OsType.toOsName(): String = when (this) {
        OsType.WINDOWS -> "windows"
        OsType.OS_X -> "osx"
        OsType.UNIX -> "linux"
    }

    // Read in the Minecraft Manifest
    val manifest = ObjectMapper().registerModule(KotlinModule())
        .readValue<ClientManifest>(YakClient.settings.clientJsonFile.toFile())

    val versionPath = YakClient.settings.minecraftPath resolve manifest.version

    // Download minecraft jar
    val minecraftPath = versionPath resolve "minecraft-${manifest.version}.jar"
    if (minecraftPath.make()) {
        val client = (manifest.downloads[ManifestDownloadType.CLIENT]
            ?: throw IllegalStateException("Invalid client.json manifest. Must have a client download available!"))
        client.toResource().copyToBlocking(minecraftPath)
    }

    // Download mappings
    val mappingsPath = versionPath resolve "minecraft-mappings-${manifest.version}.txt"
    if (mappingsPath.make()) {
        val mappings = (manifest.downloads[ManifestDownloadType.CLIENT_MAPPINGS]
            ?: throw IllegalStateException("Invalid client.json manifest. Must have a client mappings download available!"))
        mappings.toResource().copyToBlocking(mappingsPath)
    }

    val libPath = versionPath resolve YakClient.settings.minecraftLibDir
    val nativesPath = libPath resolve YakClient.settings.minecraftNativesDir

    // Load libraries, from manifest
    val libraries: List<ClientLibrary> = manifest.libraries.filter { lib ->
        val allTypes = setOf(
            OsType.OS_X, OsType.WINDOWS, OsType.UNIX
        )

        val allowableOperatingSystems = if (lib.rules.isEmpty()) allTypes.toMutableSet()
        else lib.rules.filter { it.action == LibraryRuleAction.ALLOW }.flatMapTo(HashSet()) {
            it.osName?.osNameToType()?.let(::listOf) ?: allTypes
        }

        lib.rules.filter { it.action == LibraryRuleAction.DISALLOW }.forEach {
            it.osName?.osNameToType()?.let(allowableOperatingSystems::remove)
        }

        allowableOperatingSystems.contains(OsType.type)
    }

    // Load natives from libraries
    val nativeHandles = libraries.mapNotNullBlocking { lib ->
        val descriptor = MavenDescriptor.parseDescription(lib.name) ?: return@mapNotNullBlocking null
        val (_, artifact, version, classifier) = descriptor

        if (!descriptor.isNativeLib) return@mapNotNullBlocking null

        val jarName = "$artifact-$version-${classifier}.jar"

        val nativePath = nativesPath resolve jarName
        if (nativePath.make()) {
            logger.log(Level.INFO, "Downloading minecraft native library : '$jarName'")

            lib.downloads.artifact.toResource() copyTo nativePath
        }

        Archives.find(nativePath, Archives.Finders.ZIP_FINDER)
    }

    // Resolution Results
    val results = ArrayList<JpmResolutionResult>()

    // Native source provider
    val nativeSourceProvider = object : SourceProvider {
        override val packages: Set<String> = hashSetOf()

        override fun getClass(name: String): ByteBuffer? = null

        override fun getResource(name: String): URL? =
            nativeHandles.firstNotNullOfOrNull { it.reader[name]?.resource?.uri?.toURL() }
    }

    // Dependency Resolver, will attempt to locate a module then fall back on the archive resolver.
    val resolver = YakClient.moduleResolver.orFallBackOn { handle, parents ->
        val loader = ClConglomerate(
            loader,
            listOf(ArchiveSourceProvider(handle), nativeSourceProvider),
            parents.map(::ArchiveComponent)
        )

        Archives.resolve(handle, loader, Archives.Resolvers.JPM_RESOLVER, parents).also(results::add).archive
    }

    // Repository Handler
    val repoHandler = MinecraftRepository(libraries)
    // Dependency Loader
    val dependencyLoader = DependencyGraph.ofRepository(repoHandler, resolver, DependencyCache(libPath))

    // Loads minecraft dependencies
    val minecraftDependencies = libraries
        .filter { MavenDescriptor.parseDescription(it.name)?.isNativeLib == false }
        .flatMap { dependencyLoader.load(it.name) }

    // Loads minecraft reference
    val mcReference = Archives.find(minecraftPath, Archives.Finders.ZIP_FINDER)

    // Resolves reference
    val minecraft: ResolvedArchive = Archives.resolve(
        mcReference, MinecraftLoader(
            this.loader, minecraftDependencies.map(::ArchiveComponent), mcReference
        ), Archives.Resolvers.ZIP_RESOLVER
    ).archive

    // Opens all dependency packages to minecraft
    results.forEach { result ->
        result.archive.packages.forEach { p ->
            result.controller.addOpens(result.module, p, minecraft.classloader.unnamedModule)
        }
    }
    return minecraft
}