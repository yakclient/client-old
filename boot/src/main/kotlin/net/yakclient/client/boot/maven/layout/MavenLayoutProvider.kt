package net.yakclient.client.boot.maven.layout

import net.yakclient.client.boot.repository.RepositorySettings

public interface MavenLayoutProvider {
    public fun provide(settings: RepositorySettings): MavenRepositoryLayout

    public fun provides(layout: String): Boolean
}