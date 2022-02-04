package net.yakclient.client.boot.internal

import net.yakclient.client.boot.internal.maven.MavenRepositoryHandler
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositoryProvider
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType

public class InternalRepoProvider : RepositoryProvider {
    override fun provide(settings: RepositorySettings): RepositoryHandler<*> = when(settings.type) {
        RepositoryType.MAVEN_CENTRAL, RepositoryType.MAVEN -> MavenRepositoryHandler(settings)
        RepositoryType.LOCAL -> LocalRepositoryHandler(settings)
    }

    override fun provides(type: RepositoryType): Boolean = true
}