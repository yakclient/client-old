package net.yakclient.client.api.internal

import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.maven.MAVEN
import net.yakclient.client.boot.maven.MAVEN_CENTRAL
import net.yakclient.client.boot.maven.MavenDescriptor
import net.yakclient.client.boot.maven.URL_OPTION_NAME
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings

private const val REPO_NAME = "minecraft"
private const val DELEGATE_URL = "https://libraries.minecraft.net"

internal class MinecraftRepository(
    libraries: List<ClientLibrary>
) : RepositoryHandler<MavenDescriptor> {
    private val dependencies = libraries.associateBy { MavenDescriptor.parseDescription(it.name)!!.copy(version = "") }
    private val delegate = RepositoryFactory.create(
        RepositorySettings(
            MAVEN,
            mapOf(URL_OPTION_NAME to DELEGATE_URL)
        )
    ) as RepositoryHandler<MavenDescriptor>
    private val secondaryDelegate =
        RepositoryFactory.create(RepositorySettings(MAVEN_CENTRAL)) as RepositoryHandler<MavenDescriptor>

    init {
        RepositoryFactory.add {
            if (it.type == REPO_NAME) this else null
        }
    }

    override val settings: RepositorySettings = RepositorySettings(REPO_NAME)

    override fun find(desc: MavenDescriptor): Dependency? {
        val dependency = dependencies[desc.copy(version = "")] ?: return null
        val descriptor = loadDescription(dependency.name) ?: return null
        val delegatedDependency = delegate.find(descriptor) ?: secondaryDelegate.find(descriptor) ?: return null

        val dependants = delegatedDependency.dependants.filter {
            dependencies.contains((it.desc as MavenDescriptor).copy(version = ""))
        }.mapTo(HashSet()) { Dependency.Transitive(listOf(settings), it.desc) }

        return Dependency(
            dependency.downloads.artifact.toResource(),
            dependants,
            desc
        )
    }

    override fun loadDescription(dep: String): MavenDescriptor? = delegate.loadDescription(dep)
}