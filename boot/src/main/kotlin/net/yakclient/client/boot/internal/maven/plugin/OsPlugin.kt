package net.yakclient.client.boot.internal.maven.plugin

import net.yakclient.client.boot.maven.plugin.MockMavenPlugin
import net.yakclient.client.boot.maven.plugin.MockPluginConfiguration

private const val CLASSIFIER_WITH_LIKES_PROPERTY = "os.detection.classifierWithLikes"

public class OsPlugin(configuration: MockPluginConfiguration) : MockMavenPlugin(configuration) {
    override val mockedGroup: String = MOCKED_GROUP
    override val mockedArtifact: String = MOCKED_ARTIFACT
    override val mockedVersion: VersionDescriptor = MOCKED_VERSION

    private val detector = Detector.getInstance()

    override val properties: Map<String, String> = detector.detect(configuration.pom.properties[CLASSIFIER_WITH_LIKES_PROPERTY]?.split(",") ?: listOf())

    public companion object {
        public const val MOCKED_GROUP: String = "kr.motd.maven"
        public const val MOCKED_ARTIFACT: String = "os-maven-plugin"

        public val MOCKED_VERSION: VersionDescriptor = VersionDescriptor(ALL_VERSION)
    }
}