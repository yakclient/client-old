package net.yakclient.client.boot.repository

import net.yakclient.client.boot.dependency.Dependency

public interface RepositoryHandler<D: Dependency.Descriptor> {
    public val settings: RepositorySettings

    /**
     * Finds the given dependency based on the settings provided. Should never
     * throw an exception except in edge cases(for example parsing a dependency correctly
     * but the pom not having a suitable version). If an error occurs this method should
     * return null and the caller can decide what to do.
     *
     * @param desc The dependency to find
     * @return The found dependency or null if it failed
     */
    public fun find(desc: D) : Dependency?

    public fun loadDescription(dep: String) : D?
}