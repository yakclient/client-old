package net.yakclient.client.util.test.resource

import net.yakclient.common.util.*
import net.yakclient.common.util.resource.ExternalResource
import java.io.InputStream
import java.net.URI
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.*
import kotlin.test.Test

class ExternalResourceTests {
    @Test
    fun readMessageDigest() {
        val url =
            "https://oss.sonatype.org/service/local/repositories/google-snwapshots/content/com/google/http-client/google-http-client/maven-metadata.xml"
        val stream = URI.create(url).openStream()

        val dIn = DigestInputStream(stream, MessageDigest.getInstance("SHA1"))
        dIn.use(InputStream::readInputStream)

        val second = dIn.messageDigest.digest()

        println(String(second))
        println(String( URI.create("$url.sha1").readBytes()))
    }

    @Test
    fun testResource() {
        val r1 = ExternalResource(
            URI.create("https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.6.0/kotlin-stdlib-1.6.0.jar"),
            HexFormat.of().parseHex("a40b8b22529b733892edf4b73468ce598bb17f04")
        )
        println(r1.open().readAllBytes().size)
        val r2 = ExternalResource(
            URI.create("https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-cli-jvm/0.3.4/kotlinx-cli-jvm-0.3.4.pom"),
            HexFormat.of().parseHex("cf116e0f6cb95e8fef1d7283df8d4c479ba57cd2")
        )
        println(r2.open().readAllBytes().size)
    }

    @Test
    fun testFully() {
        val url =
            "https://repo.maven.apache.org/maven2/org/jetbrains/kotlinx/kotlinx-cli-jvm/0.3.4/kotlinx-cli-jvm-0.3.4.pom"
        val resource = URI.create(url).toResource(
            URI.create("$url.sha1").readAsSha1()
        )
        println(resource.open().use { it.available() })
    }

    //https://oss.sonatype.org/service/local/repositories/google-snapshots/content/com/google/web/bindery/requestfactory/maven-metadata.xml
    //https://oss.sonatype.org/service/local/repositories/snapshots/content/cloud/aispring/common/1.0-SNAPSHOT/maven-metadata.xml
    @Test
    fun `Test snapshot repository checksum`() {
        val url =
            "https://oss.sonatype.org/service/local/repositories/google-snapshots/content/com/google/http-client/google-http-client/maven-metadata.xml"
        val resource = URI.create(url).toResource(
            URI.create("$url.sha1").readAsSha1(), "SHA1"
        )
        println(resource.open().use { it.available() })
    }
}