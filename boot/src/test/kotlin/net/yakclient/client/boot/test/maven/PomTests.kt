package net.yakclient.client.boot.test.maven

import net.yakclient.client.boot.internal.CentralMavenLayout
import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.common.util.resource.SafeResource
import kotlin.test.Test

class PomTests {
    @Test
    fun `Test property matching Regex`() {
        val regex = Regex("^\\$\\{(.*)}$")

        assert(!regex.matches("asdf"))
        assert(regex.matches("\${asdf}"))

        val r = checkNotNull(regex.find("\${test.property}"))
        println(r.groupValues[1])
    }

    @Test
    fun `Test Pom Loading`() {
        val loadMavenPom = CentralMavenLayout.parsePom(CentralMavenLayout.pomOf("com.google.guava", "guava", "31.0.1-jre"))
        println(loadMavenPom)
    }
}


