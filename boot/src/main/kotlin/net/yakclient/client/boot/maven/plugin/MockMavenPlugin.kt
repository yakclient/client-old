package net.yakclient.client.boot.maven.plugin

import net.yakclient.client.boot.maven.MavenPropertySource

public abstract class MockMavenPlugin(
    protected val configuration: MockPluginConfiguration
) : MavenPropertySource {
    public abstract val mockedGroup: String
    public abstract val mockedArtifact: String
    public open val mockedVersion: VersionDescriptor
        get() = VersionDescriptor(ALL_VERSION)

    public companion object {
        @JvmStatic
        public val ALL_VERSION: String = "<*>"
    }

    public data class VersionDescriptor(
        private val _version: String?
     ) {
        public val version: String = _version ?: ALL_VERSION

        public val isAll: Boolean = version == ALL_VERSION

        public fun matches(other: VersionDescriptor) : Boolean = other.isAll || isAll || other.version == version
    }
}