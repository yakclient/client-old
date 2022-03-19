package net.yakclient.client.api.internal

import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositoryProvider
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.*
import net.yakclient.client.util.resource.SafeResource
import java.net.URL

private const val MOJANG_REPO_TYPE = "MOJANG"

internal object MojangRepositoryHandler :
    RemoteMavenHandler(RepositorySettings(MOJANG_REPO_TYPE, "https://libraries.minecraft.net")) {
    private val repo = URL(settings.url)

    override fun metaOf(group: String, artifact: String): SafeResource? = null

    override fun find(desc: MavenDescriptor): Dependency? {
        val dep = super.find(desc) ?: return null

        return Dependency(
            dep.jar,
            dep.dependants.mapTo(HashSet()) {
                Dependency.Transitive(
                    listOf(this.settings) + it.possibleRepos,
                    it.desc
                )
            },
            dep.desc,
        )
    }

    private fun baseArtifact(c: MavenDescriptor): URL =
        repo.urlAt(c.group.replace('.', '/'), c.artifact)

    private fun versionedArtifact(c: MavenDescriptor): URL? =
        c.let { baseArtifact(c).urlAt(it.version ?: return@let null) }
}

public class MojangRepositoryProvider : RepositoryProvider {
    override fun provide(settings: RepositorySettings): RepositoryHandler<*> = MojangRepositoryHandler

    override fun provides(type: String): Boolean = type == MOJANG_REPO_TYPE
}