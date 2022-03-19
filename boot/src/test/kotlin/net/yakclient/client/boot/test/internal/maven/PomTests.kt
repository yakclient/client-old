package net.yakclient.client.boot.test.internal.maven

import net.yakclient.client.boot.maven.MavenVersionContext
import net.yakclient.client.boot.maven.Pom
import net.yakclient.client.boot.maven.loadMavenPom
import net.yakclient.client.boot.maven.mavenCentralSchema
import net.yakclient.client.util.resource.SafeResource
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
        val c = mavenCentralSchema.contextHandle
        assert(c.supply(MavenVersionContext("com.google.guava", "guava", "31.0.1-jre")))

        val loadMavenPom = loadMavenPom( c.getValue(mavenCentralSchema.pom))
        println(loadMavenPom)
    }
}


