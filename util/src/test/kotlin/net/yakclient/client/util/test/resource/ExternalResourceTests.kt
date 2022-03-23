package net.yakclient.client.util.test.resource

import net.yakclient.client.util.openStream
import net.yakclient.client.util.readHexToBytes
import net.yakclient.client.util.resource.ExternalResource
import net.yakclient.client.util.toResource
import java.net.URI
import java.security.MessageDigest
import java.util.*
import kotlin.test.Test

class ExternalResourceTests {
    @Test
    fun readMessageDigest() {
        val digest = MessageDigest.getInstance("SHA1")
        val stream =
            URI.create("https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-cli-jvm/0.3.4/kotlinx-cli-jvm-0.3.4.pom")
                .openStream()

        val bytes = stream.readAllBytes()
        digest.update(bytes)

        val first = HexFormat.of().parseHex("cf116e0f6cb95e8fef1d7283df8d4c479ba57cd2")
        println(String(first))
        val second = digest.digest()
        println(String(second))
        println(first.contentEquals(second))
    }

    @Test
    fun testResource() {
        val r1 = ExternalResource(
            URI.create("https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.6.0/kotlin-stdlib-1.6.0.jar"),
            HexFormat.of().parseHex("74581955072dfff26c6928c6296b15b2")
        )
        println(r1.open().readAllBytes().size)
        val r2 = ExternalResource(
            URI.create("https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-cli-jvm/0.3.4/kotlinx-cli-jvm-0.3.4.pom"),
            HexFormat.of().parseHex("cabd18b69c48979840b649421019c55b")
        )
        println(r2.open().readAllBytes().size)
    }

    @Test
    fun testFully() {
        val url =
            "https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-cli-jvm/0.3.4/kotlinx-cli-jvm-0.3.4.pom"
        val resource = URI.create(url).toResource(
            URI.create("$url.sha1").readHexToBytes()
        )
        println(resource.open().use { it.available() })
    }
}