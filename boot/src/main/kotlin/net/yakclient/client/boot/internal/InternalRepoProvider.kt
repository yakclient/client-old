package net.yakclient.client.boot.internal

import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.maven.layout.MavenLayoutFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositoryProvider
import net.yakclient.client.boot.repository.RepositorySettings

public class InternalRepoProvider : RepositoryProvider {
    override fun provide(settings: RepositorySettings): RepositoryHandler<*>? {
        val layout = when (settings.type) {
            MAVEN_CENTRAL -> CentralMavenLayout
            MAVEN -> MavenLayoutFactory.createLayout(settings)
            MAVEN_LOCAL -> LocalMavenLayout
            else -> return null
        }

        return MavenRepositoryHandler(layout, settings)
    }
}