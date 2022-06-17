package net.yakclient.client.boot.maven.pom

import net.yakclient.client.boot.maven.MavenRepositoryHandler

internal data class WrappedPomData(
    val pomData: PomData,
    val thisRepo: MavenRepositoryHandler
) : PomProcessStage.StageData