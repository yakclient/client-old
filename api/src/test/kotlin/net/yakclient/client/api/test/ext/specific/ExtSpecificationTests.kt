package net.yakclient.client.api.test.ext.specific

import com.typesafe.config.ConfigFactory
import net.yakclient.client.util.child
import net.yakclient.client.util.parent
import net.yakclient.client.util.workingDir
import org.junit.jupiter.api.Test

class ExtSpecificationTests {
    @Test
    fun testHoconParsing() {
        println(ConfigFactory.parseFile(workingDir().parent("client").child("api", "src", "main", "resources", "ext-settings.conf")).getStringList("runtime.specifics"))
    }
}