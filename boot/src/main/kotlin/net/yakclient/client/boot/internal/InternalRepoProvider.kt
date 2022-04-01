package net.yakclient.client.boot.internal

import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.maven.layout.MavenLayoutFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositoryProvider
import net.yakclient.client.boot.repository.RepositorySettings

public class InternalRepoProvider : RepositoryProvider {
    override fun provide(settings: RepositorySettings): RepositoryHandler<*> = MavenRepositoryHandler(
        when (settings.type) {
            MAVEN_CENTRAL -> CentralMavenLayout
            MAVEN -> MavenLayoutFactory.createLayout(settings)
            MAVEN_LOCAL -> LocalMavenLayout
            else -> throw IllegalArgumentException("Illegal repository type: ${settings.type}")
        }, settings
    )

    override fun provides(type: String): Boolean = when (type) {
        MAVEN_CENTRAL, MAVEN_LOCAL, MAVEN -> true
        else -> false
    }
}