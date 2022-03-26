package net.yakclient.client.boot.maven

import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.resource.SafeResource

public interface MavenRepositoryLayout {
    public val settings: RepositorySettings
    /**
     * Loads the pom of the given artifact into a safe resource for
     * later consumption.
     *
     * @param g The groupId
     * @param a The artifact name
     * @param v The version
     *
     * @return The SafeResource containing the loaded pom.
     */
    @Throws(InvalidMavenLayoutException::class)
    public fun pomOf(g: String, a: String, v: String): SafeResource

    /**
     * Loads the jar of the given artifact into a safe resource or
     * null if there is no jar artifact present.
     *
     * @param g The groupId
     * @param a The artifact name
     * @param v The version
     *
     * @return The SafeResource containing the jar.
     */
    public fun jarOf(g: String, a: String, v: String): SafeResource?

    /**
     * Loads the artifact meta of the given groupId and artifact. In most
     * maven repositories referred to as the `maven-metadata.xml`.
     *
     * @param g The groupId
     * @param a The artifact name
     *
     * @return The SafeResource containing the artifact meta data.
     */
    @Throws(InvalidMavenLayoutException::class)
    public fun artifactMetaOf(g: String, a: String): SafeResource
}