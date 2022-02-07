package net.yakclient.client.boot.test.repository

import net.yakclient.client.boot.dep.Dependency
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType
import kotlin.test.Test
import kotlin.test.assertNotNull

class MavenRepoTests {
    @Test
    fun `Test repository handler`() {
        val handler = RepositoryFactory.create(RepositorySettings(RepositoryType.MAVEN_CENTRAL, null)) as RepositoryHandler<Dependency.Descriptor>

        val dep = handler.find(checkNotNull(handler.loadDescription("net.bytebuddy:byte-buddy:1.12.4")) { "Failed to find dependency" })

        println(dep?.uri?.path)
        println(dep?.dependants)
    }

    @Test
    fun `Test maven local repository handler`() {
        val handler = RepositoryFactory.create(RepositorySettings(RepositoryType.MAVEN_LOCAL, null)) as RepositoryHandler<Dependency.Descriptor>

        val dep = handler.find(checkNotNull(handler.loadDescription("net.yakclient:bmu-api:1.0-SNAPSHOT")) { "Failed to find dependency" })

        println(dep?.uri?.path)
        println(dep?.dependants)
    }
}