package net.yakclient.client.api.internal.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.api.internal.ClientManifest
import net.yakclient.client.api.internal.ManifestDownloadType
import kotlin.test.Test

class ManifestParsingTests {
    @Test
    fun `Parse full manifest file`() {
        val mapper = ObjectMapper().registerModule(KotlinModule())

        val manifest = mapper.readValue<ClientManifest>(javaClass.getResourceAsStream("/1.18.1.json"))

        println(manifest.downloads[ManifestDownloadType.CLIENT]!!.url)
    }
}