package net.yakclient.client.api.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.archive.ArchiveUtils.resolve
import net.yakclient.client.boot.archive.ArchiveUtils.zipFinder
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.extension.ExtensionLoader
import net.yakclient.client.boot.loader.ArchiveConglomerateProvider
import net.yakclient.client.util.*
import net.yakclient.client.util.resource.SafeResource
import java.nio.file.Path
import java.util.*
import java.util.logging.Level
import kotlin.collections.HashSet

public class ApiInternalExt : Extension() {
    private infix fun SafeResource.copyToBlocking(to: Path): Path = runBlocking { this@copyToBlocking copyTo to }

    override fun onLoad() {
        val ext = ArchiveUtils.find(YakClient.settings.mcExtLocation, ArchiveUtils.jpmFinder)

        val manifest = ObjectMapper().registerModule(KotlinModule())
            .readValue<ClientManifest>(YakClient.settings.clientJsonFile.toFile())

        val versionPath = YakClient.settings.minecraftPath resolve manifest.version
        val minecraftPath = versionPath resolve "minecraft-${manifest.version}.jar"
        if (minecraftPath.make()) {
            val client = (manifest.downloads[ManifestDownloadType.CLIENT]
                ?: throw IllegalStateException("Invalid client.json manifest. Must have a client download available!"))
            client.url.toResource(HexFormat.of().parseHex(client.checksum)).copyToBlocking(minecraftPath)
        }

        val overriddenNames = hashMapOf<String, String>()

        val libNames: Map<String, String> = LazyMap(overriddenNames) { n ->
            n.split(':').let { "${it[1]}-${it[2]}" }
        }

        val libPath = versionPath resolve YakClient.settings.minecraftLibDir

        val nativesPath = libPath resolve YakClient.settings.minecraftNativesDir

        val mcReference = ArchiveUtils.find(minecraftPath, zipFinder)

        val dependencies = manifest.libraries
            .filter { lib ->
                fun String.osNameToType(): OsType? = when (this) {
                    "linux" -> OsType.UNIX
                    "windows" -> OsType.WINDOWS
                    "osx" -> OsType.OS_X
                    else -> null
                }
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

        val dependencyRefs = dependencies.mapBlocking {
            val path = libPath resolve "${libNames[it.name]}.jar"


            if (path.make()) {
                logger.log(Level.INFO, "Downloading minecraft dependency: '${it.name}'")

                it.downloads.artifact.url.toResource(
                    HexFormat.of().parseHex(it.downloads.artifact.checksum)
                ) copyTo path
            }

            ArchiveUtils.find(
                path,
                zipFinder
            )
        }

        fun OsType.toOsName(): String = when (this) {
            OsType.WINDOWS -> "windows"
            OsType.OS_X -> "osx"
            OsType.UNIX -> "linux"
        }

        val nativeHandles = dependencies.mapNotNullBlocking { lib ->
            val (_, artifact, version) = lib.name.split(':')

            val native = lib.natives[OsType.type.toOsName()] ?: return@mapNotNullBlocking null
            val classifier = lib.downloads.classifiers[native] ?: return@mapNotNullBlocking null

            val jarName = "$artifact-$version-${native}.jar"
            logger.log(Level.INFO, "Downloading minecraft native library : '$jarName'")

            val path = nativesPath resolve jarName

            classifier.url.toResource(HexFormat.of().parseHex(classifier.checksum)) copyTo path

            ArchiveUtils.find(path, zipFinder)
        }

        val loader = MinecraftLoader(
            this.loader,
            (dependencyRefs + mcReference + nativeHandles).map(::ArchiveConglomerateProvider),
        )

        val minecraft = resolve(dependencyRefs + mcReference) { loader }

        val settings = ExtensionLoader.loadSettings(ext)

        ExtensionLoader.load(
            ext,
            this,
            settings = settings,
            dependencies = ExtensionLoader.loadDependencies(settings)
                .let { it.toMutableList().also { m -> m.addAll(minecraft) } }).onLoad()
    }
}