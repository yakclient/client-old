package net.yakclient.client.api.internal.test

import net.yakclient.client.api.internal.OsType
import kotlin.test.Test

class TestOsType {
    @Test
    fun `Test Os Type`() {
        val type = OsType.type
        println(type)
    }
}