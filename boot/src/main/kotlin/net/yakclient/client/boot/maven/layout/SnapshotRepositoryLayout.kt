package net.yakclient.client.boot.maven.layout

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.maven.layout
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.*
import net.yakclient.client.util.resource.DownloadFailedException
import net.yakclient.client.util.resource.SafeResource

public open class SnapshotRepositoryLayout(settings: RepositorySettings) : DefaultMavenLayout(settings) {
    private val mapper: ObjectMapper = XmlMapper().registerModule(KotlinModule())

    protected fun latestClassifierVersions(resource: SafeResource): Map<String, String> {
        val tree = mapper.readValue<Map<String, Any>>(resource.open())

        val snapshotVersions = ((tree["versioning"] as? Map<String, Any>)
            ?.get("snapshotVersions") as? Map<String, Any>)?.get("snapshotVersion")

        val extension = when (snapshotVersions) {
            is Map<*, *> -> {
                snapshotVersions as Map<String, String>
                mapOf(snapshotVersions["extension"]!! to snapshotVersions["value"]!!)
            }
            is List<*> -> snapshotVersions.filterIsInstance<Map<String, String>>()
                .associate { it["extension"]!! to it["value"]!! }
            else -> null
        }

        return extension ?: throw IllegalArgumentException("Failed to parse snapshot values of pom: '${resource.uri}'")
    }


    override fun pomOf(g: String, a: String, v: String): SafeResource {
        val snapshots = latestClassifierVersions(versionMetaOf(g, a, v))
        val pomVersion = snapshots["pom"]
            ?: throw IllegalStateException("Failed to find pom snapshot version for artifact: '$g-$a-$v'")

        // TODO Find a way to put this into some function call so there are no duplicates
        val s = "${a}-${pomVersion}"
        val va = versionedArtifact(g, a, v)
        return va.uriAt("$s.pom").takeIf { it.toURL().isReachable() }
            ?.toResource(va.uriAt("$s.pom.sha1").readHexToBytes()) ?: throw InvalidMavenLayoutException(
            "$s.pom",
            settings.layout
        )
    }

    override fun jarOf(g: String, a: String, v: String): SafeResource? {
        val snapshots = latestClassifierVersions(versionMetaOf(g, a, v))
        val jarVersion = snapshots["jar"]
            ?: throw IllegalStateException("Failed to find jar snapshot version for artifact: '$g-$a-$v'")

        val s = "${a}-${jarVersion}"
        val va = versionedArtifact(g, a, v)
        return va.uriAt("$s.jar").takeIf { it.toURL().isReachable() }
            ?.toResource(va.uriAt("$s.jar.sha1").readHexToBytes())
    }

    protected fun versionMetaOf(g: String, a: String, v: String): SafeResource {
        val va = versionedArtifact(g, a, v)
        return runCatching(DownloadFailedException::class) {
            val xmlChecksum = va.uriAt("maven-metadata.xml.sha1")
            if (!xmlChecksum.toURL().isReachable()) throw InvalidMavenLayoutException("Failed to find maven-metadata.xml checksum(for version: $v) in current snapshot repository.", settings.layout)
            va.uriAt("maven-metadata.xml").toResource(xmlChecksum.readHexToBytes())
        } ?: throw InvalidMavenLayoutException("maven-metadata.xml", settings.layout)
    }
}