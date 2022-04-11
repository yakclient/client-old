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
import net.yakclient.client.boot.loader.ClConglomerate
import net.yakclient.client.util.*
import net.yakclient.client.util.resource.SafeResource
import java.nio.file.Path
import java.util.*

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

        // TODO add support for excluding based on the client manifest
//        val repoSettings = RepositorySettings(RepositoryType.MAVEN, "https://libraries.minecraft.net")
//        val internalRepo = MavenRepositoryHandler(
//            repoSettings,
//            MojangRepositoryHandler
//        )

//        val descriptorsToReplace = mapOf(
//
//        )

        // The whole point of this is make sure that the minecraft repo is included as a possible repository since the poms of the actual artifacts don't do that...
//        val repository = object : RepositoryHandler<MavenDescriptor> by internalRepo {
//            override val settings: RepositorySettings = repoSettings
//
//            override fun find(desc: MavenDescriptor): Dependency? =
//                internalRepo.find(desc)?.let {
//                    Dependency(
//                        it.jar,
//                        it.dependants.mapTo(HashSet()) { d ->
//                            Dependency.Transitive(
//                                d.possibleRepos + repoSettings,
//                                d.desc
//                            )
//                        },
//                        it.desc
//                    )
//                }
//        }

//        val

//        val depLoader = DependencyGraph.ofRepository(MojangRepositoryHandler)
//        val mcDeps: List<ResolvedArchive> = manifest.libraries.map { depLoader.load(it.name)!! }

//        val libs = DependencyGraph.ofRepository(RepositorySettings(type = MAVEN, options = mapOf(URL_OPTION_NAME to "https://libraries.minecraft.net")))


        val overriddenNames = hashMapOf<String, String>()

        val libNames: Map<String, String> = LazyMap(overriddenNames) { n ->
            n.split(':').let { "${it[1]}-${it[2]}" }
        }

        val libPath = versionPath resolve YakClient.settings.minecraftLibDir

        val nativesPath = libPath resolve YakClient.settings.minecraftNativesDir

        val mcReference = ArchiveUtils.find(minecraftPath, zipFinder)

        val dependencies = manifest.libraries.mapBlocking {
            val path = libPath resolve "${libNames[it.name]}.jar"

            if (path.make()) it.downloads.artifact.url.toResource(
                HexFormat.of().parseHex(it.downloads.artifact.checksum)
            ) copyTo path

            ArchiveUtils.find(
                path,
                zipFinder
            )
        }

        val loader = ClConglomerate(this.loader, (dependencies + mcReference).map(::ArchiveConglomerateProvider))

        val minecraft = resolve(dependencies + mcReference) { loader }

        val nativeClassifier = when (OsType.get()) {
            OsType.OS_X -> ClassifierType.NATIVES_MACOS
            OsType.WINDOWS -> ClassifierType.NATIVES_WINDOWS
            OsType.UNIX -> ClassifierType.NATIVES_LINUX
            else -> throw IllegalStateException("Unknown OS type: $this")
        }

        manifest.libraries.forEach { lib ->
            val (group, artifact, version) = lib.name.split(':')

            lib.downloads.classifiers[nativeClassifier]?.let { native ->
                val path = nativesPath resolve "$artifact-$version-$nativeClassifier.jar"
                native.url.toResource(HexFormat.of().parseHex(native.checksum))
            }
        }

        val settings = ExtensionLoader.loadSettings(ext)

        ExtensionLoader.load(
            ext,
            this,
            settings = settings,
            dependencies = ExtensionLoader.loadDependencies(settings)
                .let { it.toMutableList().also { m -> m.addAll(minecraft) } }).onLoad()
    }
}