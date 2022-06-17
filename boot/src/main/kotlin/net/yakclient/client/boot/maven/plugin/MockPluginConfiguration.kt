package net.yakclient.client.boot.maven.plugin

import net.yakclient.client.boot.maven.pom.PomData

public data class MockPluginConfiguration(
    public val values: Map<String, Any>,
    public val pom: PomData
)