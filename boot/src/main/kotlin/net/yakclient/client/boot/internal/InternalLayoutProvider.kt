package net.yakclient.client.boot.internal

import net.yakclient.client.boot.maven.DEFAULT_MAVEN_LAYOUT
import net.yakclient.client.boot.maven.SNAPSHOT_MAVEN_LAYOUT
import net.yakclient.client.boot.maven.layout
import net.yakclient.client.boot.maven.layout.DefaultMavenLayout
import net.yakclient.client.boot.maven.layout.MavenLayoutProvider
import net.yakclient.client.boot.maven.layout.MavenRepositoryLayout
import net.yakclient.client.boot.maven.layout.SnapshotRepositoryLayout
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.equalsAny

internal class InternalLayoutProvider : MavenLayoutProvider {
    override fun provide(settings: RepositorySettings): MavenRepositoryLayout = when(settings.layout) {
        DEFAULT_MAVEN_LAYOUT -> DefaultMavenLayout(settings)
        "local" -> LocalMavenLayout
        SNAPSHOT_MAVEN_LAYOUT -> SnapshotRepositoryLayout(settings)
        else -> throw IllegalArgumentException("Invalid layout passed: ${settings.layout}")
    }

    override fun provides(layout: String): Boolean = layout.equalsAny(DEFAULT_MAVEN_LAYOUT, "local", SNAPSHOT_MAVEN_LAYOUT)
}