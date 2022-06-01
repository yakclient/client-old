package net.yakclient.client.boot.repository

public fun interface RepositoryProvider {
    public fun provide(settings: RepositorySettings) : RepositoryHandler<*>?
}