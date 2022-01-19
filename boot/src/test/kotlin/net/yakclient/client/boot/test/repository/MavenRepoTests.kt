package net.yakclient.client.boot.test.repository

import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType
import org.junit.jupiter.api.Test

class MavenRepoTests {
    @Test
    fun `Test repository handler`() {
        val handler = RepositoryFactory.create(RepositorySettings(RepositoryType.MAVEN_CENTRAL, null, null))

        val dep = handler.find("net.yakclient:web-utils:1.3.1")

        println(dep?.uri?.path)
    }
}