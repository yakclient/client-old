package net.yakclient.client.api.internal

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ClientManifest(
    val mainClass: String,
    val libraries: List<ClientLibrary>,
    val downloads: Map<ManifestDownloadType, McArtifact>,
    @JsonProperty("id")
    val version: String,
)

internal enum class ManifestDownloadType {
    @JsonProperty("client")
    CLIENT,

    @JsonProperty("client_mappings")
    CLIENT_MAPPINGS,

    @JsonProperty("server")
    SERVER,

    @JsonProperty("server_mappings")
    SERVER_MAPPINGS
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ClientLibrary(
    val name: String,
    val downloads: LibraryDownloads,
    @JsonProperty("extract")
    private val _extract : LibraryExtracts?
) {
    val extract : LibraryExtracts = _extract ?: LibraryExtracts(listOf())
}

internal data class LibraryExtracts(
    val exclude: List<String>
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class LibraryDownloads(
    val artifact: McArtifact,
    @JsonProperty("classifiers")
    private val _classifiers: Map<ClassifierType, McArtifact>?
) {
    @JsonIgnore
    val classifiers: Map<ClassifierType, McArtifact> = _classifiers ?: mapOf()
}


@JsonIgnoreProperties(ignoreUnknown = true)
internal data class McArtifact(
    val url: URI,
    @JsonProperty("sha1")
    val checksum: String
)

internal enum class ClassifierType {
    @JsonProperty("javadoc")
    JAVADOC,

    @JsonProperty("natives-linux")
    NATIVES_LINUX,

    @JsonProperty("natives-macos")
    NATIVES_MACOS,

    @JsonProperty("natives-osx")
    NATIVES_OSX,

    @JsonProperty("natives-windows")
    NATIVES_WINDOWS,

    @JsonProperty("sources")
    SOURCES
}
