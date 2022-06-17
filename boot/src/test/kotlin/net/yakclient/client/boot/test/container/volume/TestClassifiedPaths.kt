package net.yakclient.client.boot.test.container.volume

import net.yakclient.client.boot.container.volume.ClassifiedPath
import java.nio.file.Path
import kotlin.test.Test

class TestClassifiedPaths {
    @Test
    fun `Test path specificity determination`() {
        val first = ClassifiedPath(Path.of("/Something/That/Is/Cool"), true)
        val second = ClassifiedPath(Path.of("/Something/That/Is/Cool/And/Awesome"), true)

        assert(second.isMoreSpecific(first))
    }
}