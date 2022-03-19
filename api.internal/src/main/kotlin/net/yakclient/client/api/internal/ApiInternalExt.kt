package net.yakclient.client.api.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.archive.ArchiveUtils.resolve
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.dependency.DependencyGraph
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.extension.ExtensionLoader
import net.yakclient.client.boot.maven.MavenDescriptor
import net.yakclient.client.boot.maven.MavenRepositoryHandler
import net.yakclient.client.boot.loader.ArchiveComponent
import net.yakclient.client.boot.loader.ArchiveLoader
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType
import net.yakclient.client.util.*
import java.util.*
import kotlin.collections.HashSet

public class ApiInternalExt : Extension() {

    override fun onLoad() {
        val ext = ArchiveUtils.find(YakClient.settings.mcExtLocation)

        val manifest = ObjectMapper().registerModule(KotlinModule())
            .readValue<ClientManifest>(YakClient.settings.clientJsonFile.toFile())

        val versionPath = YakClient.settings.minecraftPath resolve manifest.version resolve "${manifest.version}.jar"
        if (versionPath.make()) {
            val client = (manifest.downloads[ManifestDownloadType.CLIENT]
                ?: throw IllegalStateException("Invalid client.json manifest. Must have a client download available!"))
            client.url.toResource(HexFormat.of().parseHex(client.checksum)).copyTo(versionPath)
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


        val depLoader = DependencyGraph.ofRepository(MojangRepositoryHandler)
        val mcDeps: List<ResolvedArchive> = manifest.libraries.map { depLoader.load(it.name)!! }

        val reference: ArchiveReference = ArchiveUtils.find(versionPath)

        val loader = ArchiveLoader(
            loader,
            mcDeps.map(::ArchiveComponent),
            reference
        ) // ClConglomerate(loader, (mcDeps + reference).map(::ArchiveConglomerateProvider))

        val minecraft = resolve(
            reference,
            loader,
            mcDeps
        )

        val settings = ExtensionLoader.loadSettings(ext)

        ExtensionLoader.load(
            ext,
            this,
            settings = settings,
            dependencies = ExtensionLoader.loadDependencies(settings)
                .let { it.toMutableList().also { m -> m.add(minecraft) } }).onLoad()
    }
}