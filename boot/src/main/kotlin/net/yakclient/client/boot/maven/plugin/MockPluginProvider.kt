package net.yakclient.client.boot.maven.plugin

public interface MockPluginProvider {
    public fun provide(
        group: String,
        artifact: String,
        version: MockMavenPlugin.VersionDescriptor,
        configuration: MockPluginConfiguration
    ): MockMavenPlugin?
}