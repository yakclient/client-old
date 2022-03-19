package net.yakclient.client.boot.repository

public interface RepositoryProvider {
    public fun provide(settings: RepositorySettings) : RepositoryHandler<*>

    public fun provides(type: String) : Boolean
}