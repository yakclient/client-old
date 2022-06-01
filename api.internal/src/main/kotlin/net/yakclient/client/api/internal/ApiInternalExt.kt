package net.yakclient.client.api.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import net.yakclient.archives.ArchiveHandle
import net.yakclient.archives.Archives
import net.yakclient.archives.ResolutionResult
import net.yakclient.archives.ResolvedArchive
import net.yakclient.archives.jpm.JpmResolutionResult
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.dependency.*
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.extension.ExtensionLoader
import net.yakclient.client.boot.loader.ArchiveComponent
import net.yakclient.client.boot.loader.ArchiveConglomerateProvider
import net.yakclient.client.boot.loader.ClConglomerate
import net.yakclient.client.boot.maven.MAVEN
import net.yakclient.client.boot.maven.MavenDescriptor
import net.yakclient.client.boot.maven.URL_OPTION_NAME
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.common.util.*
import net.yakclient.common.util.resource.SafeResource
import java.nio.file.Path
import java.util.*
import java.util.logging.Level
import kotlin.collections.HashSet

public class ApiInternalExt : Extension() {
    private infix fun SafeResource.copyToBlocking(to: Path): Path = runBlocking { this@copyToBlocking copyTo to }

    override fun onLoad() {
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

        // Download jar
        val versionPath = YakClient.settings.minecraftPath resolve manifest.version
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

        val libraries: List<ClientLibrary> = manifest.libraries.filter { lib ->
            val allTypes = setOf(
                OsType.OS_X,
                OsType.WINDOWS,
                OsType.UNIX
            )

            val allowableOperatingSystems = if (lib.rules.isEmpty()) allTypes.toMutableSet() else lib.rules
                .filter { it.action == LibraryRuleAction.ALLOW }
                .flatMapTo(HashSet()) {
                    it.osName?.osNameToType()?.let(::listOf) ?: allTypes
                }

            lib.rules.filter { it.action == LibraryRuleAction.DISALLOW }.forEach {
                it.osName?.osNameToType()?.let(allowableOperatingSystems::remove)
            }

            allowableOperatingSystems.contains(OsType.type)
        }


        val nativeHandles = libraries.mapNotNullBlocking { lib ->
            val (_, artifact, version) = lib.name.split(':')

            val native = lib.natives[OsType.type.toOsName()] ?: return@mapNotNullBlocking null
            val classifier = lib.downloads.classifiers[native] ?: return@mapNotNullBlocking null

            val jarName = "$artifact-$version-${native}.jar"

            val nativePath = nativesPath resolve jarName
            if (nativePath.make()) {
                logger.log(Level.INFO, "Downloading minecraft native library : '$jarName'")
                classifier.toResource() copyTo nativePath
            }

            Archives.find(nativePath, Archives.Finders.ZIP_FINDER)
        }

        val results = ArrayList<JpmResolutionResult>()

        val resolver = YakClient.moduleResolver.orFallBackOn { handle, parents ->
            val loader = ClConglomerate(
                YakClient.loader,
                (nativeHandles + handle).map(::ArchiveConglomerateProvider),
                parents.map(::ArchiveComponent)
            )

            val result = Archives.resolve(handle, loader, Archives.Resolvers.JPM_RESOLVER, parents)
            results.add(result)

            result.archive
        }

        val repoHandler = MinecraftRepository(libraries)

        val minecraftRepo = DependencyGraph.ofRepository(repoHandler, resolver)

        val minecraftDependencies = libraries
            .filterNot { it.name == "java-objc-bridge" }
            .flatMap {
                minecraftRepo.load(
                    it.name,
                    DependencyGraph.DependencySettings(excludes = hashSetOf("java-objc-bridge"))
                )
            }

        val mcReference = Archives.find(minecraftPath, Archives.Finders.ZIP_FINDER)

        val minecraft: ResolvedArchive = Archives.resolve(
            mcReference,
            MinecraftLoader(
                this.loader,
                minecraftDependencies.map(::ArchiveComponent),
                mcReference
            ),
            Archives.Resolvers.ZIP_RESOLVER
        ).archive

        results.forEach { result ->
            result.archive.packages.forEach { p ->
                result.controller.addOpens(result.module, p, minecraft.classloader.unnamedModule)
            }
        }

        val ext: ArchiveHandle = Archives.find(YakClient.settings.mcExtLocation, Archives.Finders.JPM_FINDER)

        val settings = ExtensionLoader.loadSettings(ext)

        ExtensionLoader.load(
            ext,
            this,
            settings = settings,
            dependencies = ExtensionLoader.loadDependencies(settings)
                .let
                { it.toMutableSet().also { m -> m.add(minecraft) } }).onLoad()
    }
}