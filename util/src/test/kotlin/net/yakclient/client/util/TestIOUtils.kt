package net.yakclient.client.util

import org.junit.jupiter.api.Test
import java.net.URL

class TestIOUtils {
    @Test
    fun `URL to resources URI`() {
        println(URL("https://docs.oracle.com/javase/7/docs/api/javax/xml/validation").resourceAt("Schema.html"))
    }

    @Test
    fun `Test URL to URL at`() {
        println(URL("https://docs.oracle.com/javase/7/docs/").urlAt("api/javax", "xml", "validation"))
    }
}