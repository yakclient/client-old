package net.yakclient.client.boot.test.internal.internal.maven.plugin

import net.yakclient.client.boot.internal.maven.plugin.Detector
import kotlin.test.Test

class TestOSDetection {
    @Test
    fun `Test Detector`() {
        println(Detector.getInstance().detect(ArrayList()))
    }
}