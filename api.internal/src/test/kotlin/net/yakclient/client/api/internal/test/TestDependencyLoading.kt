package net.yakclient.client.api.internal.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.api.internal.ClientManifest
import net.yakclient.client.boot.InitScope
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.dependency.DependencyGraph
import net.yakclient.client.boot.init
import net.yakclient.client.boot.maven.MAVEN
import net.yakclient.client.boot.maven.URL_OPTION_NAME
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.child
import net.yakclient.client.util.parent
import net.yakclient.client.util.workingDir
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestDependencyLoading {
    @BeforeTest
    fun init() {
        init(workingDir().parent("client").child("workingDir").toPath(), InitScope.DEVELOPMENT)
    }

    @Test
    fun `Test loading minecraft dependencies`() {
        val manifest = ObjectMapper().registerModule(KotlinModule())
            .readValue<ClientManifest>(YakClient.settings.clientJsonFile.toFile())

        val loader = DependencyGraph.ofRepository(
            RepositorySettings(
                type = MAVEN,
                options = mapOf(URL_OPTION_NAME to "https://libraries.minecraft.net")
            )
        )

        manifest.libraries.forEach {
            loader.load(it.name)
        }
    }
}