package net.yakclient.client.boot.maven.layout

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.maven.layout
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.common.util.*
import net.yakclient.common.util.resource.DownloadFailedException
import net.yakclient.common.util.resource.SafeResource

public class SnapshotRepositoryLayout(settings: RepositorySettings) : DefaultMavenLayout(settings) {
    private val mapper: ObjectMapper = XmlMapper().registerModule(KotlinModule())

    private data class ArtifactAddress(
        val classifier: String?,
        val type: String
    )

    private fun latestSnapshotVersions(resource: SafeResource): Map<ArtifactAddress, String> {
        val tree = mapper.readValue<Map<String, Any>>(resource.open())

        val snapshotVersions =
            ((tree["versioning"] as? Map<String, Any>)?.get("snapshotVersions") as? Map<String, Any>)?.get("snapshotVersion")

        val extension = when (snapshotVersions) {
            is Map<*, *> -> {
                @Suppress(CAST) snapshotVersions as Map<String, String>

                mapOf(
                    ArtifactAddress(
                        snapshotVersions["classifier"],
                        snapshotVersions["extension"]!!
                    ) to snapshotVersions["value"]!!
                )
            }
            is List<*> -> snapshotVersions.filterIsInstance<Map<String, String>>()
                .associate { ArtifactAddress(it["classifier"], it["extension"]!!) to it["value"]!! }
            else -> null
        }

        return extension ?: throw IllegalArgumentException("Failed to parse snapshot values of pom: '${resource.uri}'")
    }


//    override fun pomOf(g: String, a: String, v: String): SafeResource {
//        val snapshots = latestClassifierVersions(versionMetaOf(g, a, v))
//        val pomVersion = snapshots["pom"]
//            ?: throw IllegalStateException("Failed to find pom snapshot version for artifact: '$g-$a-$v'")
//
//        val s = "${a}-${pomVersion}"
//        return versionedArtifact(g, a, v).resourceAt("$s.pom") ?: throw InvalidMavenLayoutException(
//            "$s.pom",
//            settings.layout
//        )
//    }
//
//    override fun archiveOf(g: String, a: String, v: String): SafeResource {
//        val snapshots = latestClassifierVersions(versionMetaOf(g, a, v))
//        val jarVersion = snapshots["jar"]
//            ?: throw IllegalStateException("Failed to find jar snapshot version for artifact: '$g-$a-$v'")
//
//        return versionedArtifact(g, a, v).resourceAt("${a}-${jarVersion}.jar")
//            ?: throw InvalidMavenLayoutException("${a}-${jarVersion}.jar", settings.layout)
//    }

    override fun artifactOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): SafeResource? {
        val snapshots = latestSnapshotVersions(versionMetaOf(groupId, artifactId, version))
        val artifactVersion = snapshots[ArtifactAddress(classifier, type)] ?: return null
//            ?: throw IllegalStateException("Failed to find artifact snapshot version for: '$groupId:$artifactId:$version:$classifier'")

        val s = "${artifactId}-${artifactVersion}${classifier?.let { "-$it" } ?: ""}.$type"
        return versionedArtifact(groupId, artifactId, version).resourceAt(s)
    }

    protected fun versionMetaOf(g: String, a: String, v: String): SafeResource {
        return runCatching(DownloadFailedException::class) {
            versionedArtifact(g, a, v).resourceAt("maven-metadata.xml") ?: throw InvalidMavenLayoutException(
                "Failed to find maven metadata for artifact: '$g-$a-$a'",
                settings.layout
            )
        } ?: throw InvalidMavenLayoutException("maven-metadata.xml", settings.layout)
    }
}