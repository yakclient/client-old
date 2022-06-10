package net.yakclient.client.boot.test.container.volume

import net.yakclient.client.boot.InitScope
import net.yakclient.client.boot.container.volume.VolumeStore
import net.yakclient.client.boot.init
import net.yakclient.client.boot.internal.volume.absoluteRoot
import net.yakclient.client.util.child
import net.yakclient.client.util.parent
import net.yakclient.client.util.workingDir
import java.nio.file.Files
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestVolumeStore {
    @BeforeTest
    fun setup() {
        init(workingDir().parent("client").child("workingDir").toPath(), InitScope.TEST)
    }

    @Test
    fun `Test create volume`() {
        val volume = VolumeStore["my-volume"]

        assert(Files.exists(volume.absoluteRoot()))
    }
}