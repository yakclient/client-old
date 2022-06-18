package net.yakclient.client.boot.maven.pom

import net.yakclient.client.boot.internal.CentralMavenLayout
import net.yakclient.client.boot.maven.MavenRepositoryHandler
import net.yakclient.client.boot.maven.SUPER_POM
import net.yakclient.client.boot.maven.layout.MavenLayoutFactory
import net.yakclient.client.boot.maven.layout.MavenRepositoryLayout
import net.yakclient.client.boot.maven.parseData
import net.yakclient.client.boot.maven.url
import net.yakclient.client.boot.repository.RepositorySettings

internal class ParentResolutionStage : PomProcessStage<WrappedPomData, ParentResolutionStage.ParentResolutionData> {
    override fun process(i: WrappedPomData): ParentResolutionData {
        fun recursivelyLoadParents(child: PomData, thisLayout: MavenRepositoryLayout): List<PomData> {
            val parent: PomParent = child.parent ?: return listOf(SUPER_POM)

            val immediateRepos = listOf(thisLayout, CentralMavenLayout) + child.repositories
                .map(PomRepository::toSettings)
                .map(MavenLayoutFactory::createLayout)

           val artifact = immediateRepos.firstNotNullOfOrNull {
                it.artifactOf(
                    parent.groupId,
                    parent.artifactId,
                    parent.version,
                    null,
                    "pom"
                )
            } ?: throw IllegalStateException(
                "Failed to find parent: '${parent.groupId}:${parent.artifactId}:${parent.version}' in repositories: ${
                    immediateRepos
                        .map(MavenRepositoryLayout::settings)
                        .map(RepositorySettings::url) + thisLayout.settings + CentralMavenLayout.settings
                }"
            )

            val parentData = parseData(artifact)

            return listOf(parentData) + recursivelyLoadParents(parentData, thisLayout)
        }

        return ParentResolutionData(i.pomData, i.thisRepo, recursivelyLoadParents(i.pomData, i.thisRepo.layout))
    }

    data class ParentResolutionData(
        val pomData: PomData,
        val thisRepo: MavenRepositoryHandler,
        val parents: List<PomData>
    ) : PomProcessStage.StageData
}