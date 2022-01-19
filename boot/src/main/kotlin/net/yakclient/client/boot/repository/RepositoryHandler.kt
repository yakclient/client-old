package net.yakclient.client.boot.repository

public interface RepositoryHandler {
    public val settings: RepositorySettings

    public fun find(it: String) : Dependency?
}