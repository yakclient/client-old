package net.yakclient.client.api.internal.test

import net.yakclient.client.api.internal.MojangRepositoryHandler
import kotlin.test.Test

class TestMinecraftLibSchema {
    @Test
    fun `Test parsing a Minecraft dependency from the mojang maven repository`() {
        val dep = MojangRepositoryHandler.find(MojangRepositoryHandler.loadDescription("org.lwjgl:lwjgl-openal:3.2.2")!!)
        println(dep)
    }
}