package net.yakclient.client.boot.test.repository

import net.yakclient.client.boot.InitScope
import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.child
import net.yakclient.client.util.parent
import net.yakclient.client.util.workingDir
import kotlin.test.BeforeTest
import kotlin.test.Test

class MavenRepoTests {
    @BeforeTest
    fun init() {
        net.yakclient.client.boot.init(workingDir().parent("client").child("workingDir").toPath(), InitScope.TEST)
    }

    @Test
    fun `Test repository handler`() {
        val handler =
            RepositoryFactory.create(RepositorySettings(MAVEN_CENTRAL)) as RepositoryHandler<Dependency.Descriptor>

        val dep =
            handler.find(checkNotNull(handler.loadDescription("net.bytebuddy:byte-buddy:1.12.4")) { "Failed to find dependency" })

        println(dep?.jar?.uri)
        println(dep?.dependants)
    }

    @Test
    fun `Test maven local repository handler`() {
        val handler =
            RepositoryFactory.create(RepositorySettings(MAVEN_LOCAL)) as RepositoryHandler<Dependency.Descriptor>

        val dep = handler.find(checkNotNull(handler.loadDescription("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")) { "Failed to find dependency" })

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
                    URL_OPTION_NAME to "http://repo.yakclient.net/snapshots"
                )
            )
        ) as RepositoryHandler<Dependency.Descriptor>
        val dep =
            handler.find(checkNotNull(handler.loadDescription("net.yakclient:archives:1.0-SNAPSHOT")) { "Failed to find dependency" })

        println(dep?.jar)
        println(dep?.dependants)
    }
}