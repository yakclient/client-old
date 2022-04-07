package net.yakclient.client.boot.maven

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.internal.CentralMavenLayout
import net.yakclient.client.boot.maven.layout.InvalidMavenLayoutException
import net.yakclient.client.boot.maven.layout.MavenLayoutFactory
import net.yakclient.client.boot.maven.layout.MavenRepositoryLayout
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.resource.SafeResource
import net.yakclient.client.util.runCatching

private val mapper = XmlMapper().registerModule(KotlinModule())

private val propertyMatcher = Regex("^\\$\\{(.*)}$")

private fun String.matchAsProperty(): String? = propertyMatcher.matchEntire(this)?.groupValues?.get(1)

internal fun MavenRepositoryLayout.parsePom(resource: SafeResource): CompressedPom = parsePom(parseData(resource), this)

internal fun parseData(resource: SafeResource): PomData = mapper.readValue(resource.open())


// TODO I really dont want to redo this again, but at some point redoing like it is actually done in maven would be a good idea. For example: https://maven.apache.org/ref/3.3.9/maven-model-builder/
internal fun parsePom(data: PomData, thisLayout: MavenRepositoryLayout): CompressedPom {
    fun toSettings(repo: PomRepository): RepositorySettings = RepositorySettings(
        MAVEN,
        options = mapOf(
            URL_OPTION_NAME to repo.url,
            LAYOUT_OPTION_NAME to (repo.layout ?: DEFAULT_MAVEN_LAYOUT)
        )
    )

    val immediateRepos = data.repositories.map(::toSettings)
    val layouts = listOf(thisLayout, CentralMavenLayout) + immediateRepos.map(MavenLayoutFactory::createLayout)

    fun pomOf(g: String, a: String, v: String) =
        layouts.firstNotNullOfOrNull { l -> runCatching(InvalidMavenLayoutException::class) { l.pomOf(g, a, v) } }
            ?: throw IllegalArgumentException(
                "Failed to find artifact: '$g:$a:$v' in repositories: ${
                    immediateRepos.map(
                        RepositorySettings::url
                    ) + thisLayout.settings + CentralMavenLayout.settings
                }"
            )

    val parents = run {
        fun recursivelyLoadParents(thisData: PomData): List<PomData> {
            val p = thisData.parent ?: return listOf()

            val resource = pomOf(p.groupId, p.artifactId, p.version)
            val parentData = parseData(resource)

            return listOf(parentData) + recursivelyLoadParents(parentData)
        }

        recursivelyLoadParents(data)
    }

    val all = listOf(data) + parents

    val (groupId, artifactId, version) = listOf(
        data.groupId ?: parents.firstNotNullOf { it.groupId },
        data.artifactId,
        data.version ?: parents.firstNotNullOf { it.version }
    )

    fun getDefaultProperty(name: String): String? = when (name) {
        "project.artifactId" -> artifactId
        "project.version" -> version
        "project.groupId" -> groupId
        "project.parent.artifactId" -> parents.first().artifactId
        "project.parent.version" -> parents.firstNotNullOf { it.version }
        else -> null
    }


    val properties = parents
        .flatMap { it.properties.asSequence() }
        .associate { it.key to it.value }


    fun String.ifAsProperty(): String =
        matchAsProperty()?.let { properties[it] ?: getDefaultProperty(it) ?: throw IllegalStateException("Given property: '$this' needs a property value substituted however no suitable one could be found!") }?.ifAsProperty() ?: this

    val descriptor = MavenDescriptor(groupId.ifAsProperty(), artifactId.ifAsProperty(), version.ifAsProperty())

    val repositories = listOf(
        thisLayout.settings,
        CentralMavenLayout.settings
    ) + immediateRepos + parents.flatMap { it.repositories.map(::toSettings) }

    fun toPomDependency(managedDependency: ManagedDependency): PomDependency = PomDependency(
        managedDependency.groupId.ifAsProperty(),
        managedDependency.artifactId.ifAsProperty(),
        managedDependency.version.ifAsProperty(),
        managedDependency.scope?.ifAsProperty() ?: "compile"
    )

    val boms = all.flatMap { it.dependencyManagement.dependencies }.filter { it.scope == "import" }
        .map { parseData(pomOf(it.groupId.ifAsProperty(), it.artifactId.ifAsProperty(), it.version.ifAsProperty())) }

    val managedDependencies = all.flatMap {
        it.dependencyManagement.dependencies.map(::toPomDependency)
    }.filterNot { it.scope == "import" } + boms.flatMap { it.dependencyManagement.dependencies.map(::toPomDependency) }


//    fun toPomDependency(it: MavenDependency) = PomDependency(
//        it.groupId.ifAsProperty(),
//        it.artifactId.ifAsProperty(),
////        it.version
//    )
//
    val dependencies = all.flatMap { datum ->
        datum.dependencies.map { dep ->
            val depGroup = dep.groupId.ifAsProperty()
            val depArtifact = dep.artifactId.ifAsProperty()

            val backup =
                managedDependencies.firstOrNull { md -> md.groupId == depGroup && md.artifactId == depArtifact }
            PomDependency(
                depGroup,
                depArtifact,
                (dep.version ?: backup?.version!!).ifAsProperty(),
                (dep.scope ?: backup?.scope)?.ifAsProperty() ?: "compile"
            )
        }
    }

    return CompressedPom(
        descriptor,
        repositories,
        dependencies,
        data.packaging
    )
}
//internal parseRepositories()


public data class CompressedPom(
    val desc: Dependency.Descriptor,
    val repositories: List<RepositorySettings>,
    val dependencies: List<PomDependency>,
    val packaging: String
)

public data class PomDependency(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val scope: String,
)
