package net.yakclient.client.boot.internal

import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositoryProvider
import net.yakclient.client.boot.repository.RepositorySettings

public class InternalRepoProvider : RepositoryProvider {
    override fun provide(settings: RepositorySettings): RepositoryHandler<*> = when(settings.type) {
        MAVEN_CENTRAL -> MavenRepositoryHandler(CentralMavenLayout, settings) //RemoteMavenHandler(RepositorySettings(MAVEN, mavenCentral))// MavenSchemaRepositoryHandler(RepositorySettings(MAVEN, mavenCentral), mavenCentralSchema)
        MAVEN -> MavenRepositoryHandler(MavenLayoutFactory.createLayout(settings), settings) //settings, RemoteMavenSchema(settings.url ?: throw IllegalArgumentException("URL cannot be null for repository of type: ${settings.type}")))
        MAVEN_LOCAL -> MavenRepositoryHandler(LocalMavenLayout, settings)
        else -> throw IllegalArgumentException("Illegal repository type: ${settings.type}")
    }

    override fun provides(type: String): Boolean = when(type) {
        MAVEN_CENTRAL, MAVEN_LOCAL, MAVEN -> true
        else -> false
    }
}