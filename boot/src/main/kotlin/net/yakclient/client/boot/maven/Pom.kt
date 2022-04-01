package net.yakclient.client.boot.maven

import net.yakclient.client.boot.repository.RepositorySettings

internal interface Pom {
    val parent: Pom?
    val desc: MavenDescriptor
    val properties: Map<String, String>
    val repositories: List<RepositorySettings>
    val dependencies: Set<MavenDependency>
    val packaging: String

    fun findProperty(name: String): String?
}

internal data class ChildPom(
    private val data: PomData,
    override val parent: Pom?
) : Pom {
    override val desc: MavenDescriptor by lazy {
        val (g, a, v) = data

        MavenDescriptor(
            g ?: parent?.desc?.group ?: throw IllegalArgumentException("Failed to find GroupId for data: $data"),
            a,
            v ?: parent?.desc?.version ?: throw IllegalArgumentException("Failed to find Version for data: $data")
        )
    }
    override val properties: Map<String, String> by lazy {
        (parent?.properties ?: mapOf()) + data.properties
    }
    override val repositories: List<RepositorySettings> by lazy {
        (parent?.repositories ?: listOf()) + (data.repositories
            ?.map {
                RepositorySettings(
                    MAVEN,
                    mapOf(URL_OPTION_NAME to it.url, LAYOUT_OPTION_NAME to (it.layout ?: DEFAULT_MAVEN_LAYOUT))
                )
            }
            ?: listOf())
    }
    override val dependencies: Set<MavenDependency> by lazy {
        (parent?.dependencies ?: setOf()) + data.dependencies.mapTo(HashSet()) { (g, a, v, s) ->
            MavenDependency(
                g,
                a,
                v,
                s
            )
        }
    }
    override val packaging: String = data.packaging

    override fun findProperty(name: String): String? = properties[name] ?: parent?.findProperty(name)
}

internal object TheSuperPom : Pom {
    override val parent: Pom? = null
    override val desc: MavenDescriptor
        get() = throw IllegalStateException("Cannot query the descriptor of the super pom! It should already be known in the pom hierarchy.")
    override val properties: Map<String, String> = mapOf()
    override val repositories: List<RepositorySettings> = listOf(RepositorySettings(MAVEN_CENTRAL))
    override val dependencies: Set<MavenDependency> = setOf()
    override val packaging: String = "pom"

    override fun findProperty(name: String): String? = null
}