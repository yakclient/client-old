package net.yakclient.client.boot.maven

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.yakclient.client.boot.repository.RepositorySettings
import java.net.URL

internal interface Pom {
    val parent: Pom?
    val desc: MavenDescriptor
    val properties: Map<String, String>
    val repositories: List<String>
    val dependencies: Set<MavenDependency>

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
        (parent?.properties ?: mapOf()) + (data.properties ?: mapOf())
    }
    override val repositories: List<String> by lazy {
        (parent?.repositories ?: listOf()) + (data.repositories
            ?.map(PomRepository::url)
            ?: listOf())
    }
    override val dependencies: Set<MavenDependency> by lazy {
        (parent?.dependencies ?: setOf()) + (data.dependencies?.mapTo(HashSet()) { (g, a, v, s) ->
            MavenDependency(
                g,
                a,
                v,
                s
            )
        } ?: setOf())
    }

    override fun findProperty(name: String): String? = properties[name] ?: parent?.findProperty(name)
}

internal object TheSuperPom : Pom {
    override val parent: Pom? = null
    override val desc: MavenDescriptor
        get() = throw IllegalStateException("Cannot query the descriptor of the super pom! It should already be known in the pom hierarchy.")
    override val properties: Map<String, String> = mapOf()
    override val repositories: List<String> = listOf(mavenCentral)
    override val dependencies: Set<MavenDependency> = setOf()

    override fun findProperty(name: String): String? = null
}