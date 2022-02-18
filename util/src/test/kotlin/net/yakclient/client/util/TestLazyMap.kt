package net.yakclient.client.util

import kotlin.test.Test

class TestLazyMap  {
    @Test
    fun `Test Lazy Map`() {
        val map = LazyMap<String, String> {
            println("Lazily evaluating")
            it.replace("a", "").replace(" ", "  ")
        }

        println(map["hello can you see this?"])

        println(map["key"])
        println(map["key"])
    }
}