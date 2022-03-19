package net.yakclient.client.boot.internal

import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositoryProvider
import net.yakclient.client.boot.repository.RepositorySettings



public class InternalRepoProvider : RepositoryProvider {
    override fun provide(settings: RepositorySettings): RepositoryHandler<*> = when(settings.type) {
        MAVEN_CENTRAL -> RemoteMavenHandler(RepositorySettings(MAVEN, mavenCentral))// MavenSchemaRepositoryHandler(RepositorySettings(MAVEN, mavenCentral), mavenCentralSchema)
        MAVEN -> RemoteMavenHandler(RepositorySettings(settings.type, settings.url?: throw IllegalArgumentException("URL cannot be null for repository of type: ${settings.type}"))) //settings, RemoteMavenSchema(settings.url ?: throw IllegalArgumentException("URL cannot be null for repository of type: ${settings.type}")))
        MAVEN_LOCAL -> LocalMavenHandler
        else -> throw IllegalArgumentException("Illegal repository type: ${settings.type}")
    }

    override fun provides(type: String): Boolean = when(type) {
        MAVEN_CENTRAL, MAVEN_LOCAL, MAVEN -> true
        else -> false
    }
}