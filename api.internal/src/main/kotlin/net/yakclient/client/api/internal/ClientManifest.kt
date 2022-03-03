package net.yakclient.client.api.internal

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI

internal data class ClientManifest(
    var mainClass: String,
    var libraries: List<ClientLibrary>
)

internal data class ClientLibrary(
    val name: String,
    val url: URI,
    val checksum: ByteArray
) {
    @JsonProperty("url")
    @Suppress("UNCHECKED_CAST")
    private fun unpackUrl(lib: Map<String, Any>): String? = (lib["downloads"] as Map<String, String>)["url"]

    @JsonProperty("checksum")
    @Suppress("UNCHECKED_CAST")
    private fun unpackChecksum(check: Map<String, Any>): String? = (check["downloads"] as Map<String, String>)["sha1"]
}
