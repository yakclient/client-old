package net.yakclient.client.api.internal

import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.maven.MAVEN
import net.yakclient.client.boot.maven.MavenDescriptor
import net.yakclient.client.boot.maven.URL_OPTION_NAME
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings

internal class MinecraftRepository(
    libraries: List<ClientLibrary>
): RepositoryHandler<MavenDescriptor> {
    private val dependencies = libraries.associateBy { MavenDescriptor.parseDescription(it.name)!!.artifact }

    init {
        RepositoryFactory.add {
            if (it.type == "minecraft") this else null
        }
    }

    private val delegate = RepositoryFactory.create(
        RepositorySettings(
            MAVEN,
            mapOf(URL_OPTION_NAME to "https://libraries.minecraft.net")
        )
    ) as RepositoryHandler<MavenDescriptor>
    override val settings: RepositorySettings = RepositorySettings("minecraft")

    override fun find(desc: MavenDescriptor): Dependency? {
        val dependency = dependencies[desc.artifact] ?: return null
        val delegatedDependency = delegate.find(loadDescription(dependency.name) ?: return null) ?: return null

        val dependants = delegatedDependency.dependants.filter {
            dependencies.contains(it.desc.artifact)
        }.mapTo(HashSet()) { Dependency.Transitive(listOf(settings), it.desc) }

        return Dependency(
            dependency.downloads.artifact.toResource(),
            dependants,
            desc
        )
    }

    override fun loadDescription(dep: String): MavenDescriptor? = delegate.loadDescription(dep)
}