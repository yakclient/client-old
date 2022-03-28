package net.yakclient.client.util.test.resource

import net.yakclient.client.util.*
import net.yakclient.client.util.resource.ExternalResource
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.HttpClients
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.net.http.HttpHeaders
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
    fun `Read the xml file`() {
        val client = HttpClients.custom().build()
        val req = RequestBuilder.get()
            .setUri("https://oss.sonatype.org/service/local/repositories/google-snapshots/content/com/google/http-client/google-http-client/1.5.2-beta-SNAPSHOT/maven-metadata.xml")
//            .setHeader(CONTENT_TYPE, "application/json")
            .build()

        println(String(client.execute(req).entity.content.readAllBytes()))
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
            URI.create("$url.sha1").readHexToBytes()
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
            URI.create("$url.sha1").readHexToBytes(), "SHA1"
        )
        println(resource.open().use { it.available() })
    }

}