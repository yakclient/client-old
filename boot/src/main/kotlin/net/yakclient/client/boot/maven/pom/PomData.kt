package net.yakclient.client.boot.maven.pom

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.yakclient.client.boot.maven.DEFAULT_MAVEN_LAYOUT
import net.yakclient.client.boot.maven.LAYOUT_OPTION_NAME
import net.yakclient.client.boot.maven.MAVEN
import net.yakclient.client.boot.maven.URL_OPTION_NAME
import net.yakclient.client.boot.repository.RepositorySettings

@JsonIgnoreProperties(ignoreUnknown = true)
public data class MavenDependency(
    val groupId: String,
    val artifactId: String,
    val version: String?,
    val classifier: String?,
    val scope: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
public data class ManagedDependency(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val classifier: String?,
    val scope: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
public data class PomParent(
    val groupId: String,
    val artifactId: String,
    val version: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
public data class PomData(
    val groupId: String?,
    val artifactId: String = "<SUPER_POM>",
    val version: String?,
    val properties: Map<String, String> = mapOf(),
    val parent: PomParent?,
    val dependencyManagement: DependencyManagement = DependencyManagement(),
    val dependencies: Set<MavenDependency> = setOf(),
    val repositories: List<PomRepository> = listOf(),
    val build: PomBuild = PomBuild(),
    val packaging: String = "jar"
)

@JsonIgnoreProperties(ignoreUnknown = true)
public data class DependencyManagement(
    val dependencies: Set<ManagedDependency> = setOf()
)

@JsonIgnoreProperties(ignoreUnknown = true)
public data class PomRepository(
    val url: String,
    val layout: String?
) {
    public fun toSettings(): RepositorySettings = RepositorySettings(
        MAVEN,
        options = mapOf(
            URL_OPTION_NAME to url,
            LAYOUT_OPTION_NAME to (layout ?: DEFAULT_MAVEN_LAYOUT)
        )
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
public data class PomBuild(
    val extensions: List<PomExtension> = listOf(),
    val plugins: List<PomPlugin> = listOf(),
    val pluginManagement: PomPluginManagement = PomPluginManagement()
)

public data class PomPluginManagement(
    val plugins: List<PomPlugin> = listOf()
)

private const val DEFAULT_PLUGIN_GROUP = "org.apache.maven.plugins"

@JsonIgnoreProperties(ignoreUnknown = true)
public data class PomPlugin(
    val groupId: String = DEFAULT_PLUGIN_GROUP,
    val artifactId: String,
    val version: String?,
    val extensions: Boolean?,
    val configurations: Map<String, Any> = mapOf()
)

@JsonIgnoreProperties(ignoreUnknown = true)
public data class PomExtension(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val configurations: Map<String, Any> = mapOf()
)