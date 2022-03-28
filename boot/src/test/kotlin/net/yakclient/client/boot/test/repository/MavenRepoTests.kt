package net.yakclient.client.boot.test.repository

import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import kotlin.test.Test

class MavenRepoTests {
    @Test
    fun `Test repository handler`() {
        val handler =
            RepositoryFactory.create(RepositorySettings(MAVEN_CENTRAL)) as RepositoryHandler<Dependency.Descriptor>

        val dep =
            handler.find(checkNotNull(handler.loadDescription("net.bytebuddy:byte-buddy:1.12.4")) { "Failed to find dependency" })

        println(dep?.jar)
        println(dep?.dependants)
    }

    @Test
    fun `Test maven local repository handler`() {
        val handler =
            RepositoryFactory.create(RepositorySettings(MAVEN_LOCAL)) as RepositoryHandler<Dependency.Descriptor>

        val dep =
            handler.find(checkNotNull(handler.loadDescription("net.yakclient:bmu-api:1.0-SNAPSHOT")) { "Failed to find dependency" })

        println(dep?.jar)
        println(dep?.dependants)
    }

    @Test
    fun `Test snapshot repository`() {
        val handler = RepositoryFactory.create(
            RepositorySettings(
                MAVEN,
                options = mapOf(
                    LAYOUT_OPTION_NAME to SNAPSHOT_MAVEN_LAYOUT,
                    URL_OPTION_NAME to "https://oss.sonatype.org/service/local/repositories/google-snapshots/content"
                )
            )
        ) as RepositoryHandler<Dependency.Descriptor>
        val dep =
            handler.find(checkNotNull(handler.loadDescription("com.google.http-client:google-http-client:1.5.2-beta-SNAPSHOT")) { "Failed to find dependency" })

        println(dep?.jar)
        println(dep?.dependants)
    }

}