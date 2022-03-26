package net.yakclient.client.boot.internal

import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.maven.DEFAULT_MAVEN_LAYOUT
import net.yakclient.client.boot.maven.layout
import net.yakclient.client.boot.repository.RepositorySettings

internal class InternalLayoutProvider : MavenLayoutProvider {
    override fun provide(settings: RepositorySettings): MavenRepositoryLayout = when(settings.layout) {
        DEFAULT_MAVEN_LAYOUT -> DefaultMavenLayout(settings)
        "local" -> LocalMavenLayout
        else -> throw IllegalArgumentException("Invalid layout passed: ${settings.layout}")
    }

    override fun provides(layout: String): Boolean = layout == DEFAULT_MAVEN_LAYOUT || layout == "local"
}