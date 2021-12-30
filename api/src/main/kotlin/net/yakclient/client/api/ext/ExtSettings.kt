package net.yakclient.client.api.ext

public data class ExtSettings(
    val id: ExtensionID,
    val name: String,
    val authors: List<ExtensionAuthor>,
    val url: String,
    val dependencies: List<ExtensionDependency>,
    val nickname: String,
    val libraries: List<String>
)

public data class ExtensionID(
    val group: String,
    val artifactName: String,
    val version: String,
)

public data class ExtensionAuthor(
    val name: String,
)

public data class ExtensionDependency(
    val id: ExtensionID,
    val type: DependencyType
) {
    public enum class DependencyType {
        REQUIRE,
        OPTIONAL
    }
}