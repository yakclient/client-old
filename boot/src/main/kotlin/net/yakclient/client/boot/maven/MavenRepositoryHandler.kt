package net.yakclient.client.boot.maven

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.LazyMap
import net.yakclient.client.util.resource.SafeResource
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger


public abstract class MavenRepositoryHandler(
    override val settings: RepositorySettings,
//    private val schema: MavenSchema
) : RepositoryHandler<MavenDescriptor> {
    private val logger = Logger.getLogger(this::class.simpleName)
    private val propertyMatcher = Regex("^\\$\\{(.*)}$")
    private val xml: ObjectMapper = XmlMapper().registerModule(KotlinModule())


    override fun find(desc: MavenDescriptor): Dependency? =
        findInternal(desc)

//    private fun substituteVersion(desc: MavenDescriptor): MavenDescriptor? {
//        if (desc.version != null) return desc
//
//        val context = schema.contextHandle
//        if (!context.supply(MavenArtifactContext(desc.group, desc.artifact))) return null
//
//        val meta by context[schema.meta]
//
//        val tree = xml.readValue<Map<String, Any>>(meta.open())
//
//        val version =
//            (tree["version"] as? String) ?: (tree["versioning"] as Map<String, String>)["release"] ?: return null
//
//        return MavenDescriptor(desc.group, desc.artifact, version)
//    }

    protected abstract fun metaOf(group: String, artifact: String) : SafeResource?

    protected abstract fun pomOf(desc: MavenDescriptor) : SafeResource?

    protected abstract fun jarOf(desc: MavenDescriptor) : SafeResource?

    protected open fun newestVersionOf(group: String, artifact: String) : MavenDescriptor? {
        val meta = metaOf(group, artifact) ?: return null

        val tree = xml.readValue<Map<String, Any>>(meta.open())

        val version = (tree["version"] as? String)
            ?: (tree["versioning"] as Map<String, String>)["release"]
            ?: return null

        return MavenDescriptor(group, artifact, version)
    }

    private fun findInternal(_desc: MavenDescriptor): Dependency? {
        val desc = if (_desc.version == null) newestVersionOf(_desc.group, _desc.artifact) ?: return null else _desc
        logger.log(Level.FINEST, "Loading maven dependency: '$desc'")

//        fun loadPomDependencies(pom: PomData): List<MavenDependency> {
//            return pom.dependencies?.map { dep ->
//                val depGroup = dep.groupId
//                val depArtifact = dep.artifactId
//
//                fun loadProperty(name: String): String? = loadProperty(pom, name)
//
//                fun loadIfAsProperty(value: String): String =
//                    if (value.startsWith("\${") && value.endsWith("}")) {
//                        loadIfAsProperty(
//                            loadProperty(value.substring(2, value.length - 1))
//                                ?: throw IllegalArgumentException("Expected to find property for value: $value in artifact: '$depGroup:$depArtifact' but instead found null")
//                        )
//                    } else value
//
//                MavenDependency(
//                    loadIfAsProperty(depGroup),
//                    loadIfAsProperty(depArtifact),
//                    dep.version?.let(::loadIfAsProperty) ?: substituteVersion(dep.toDescriptor())?.version,
//                    dep.scope?.let(::loadIfAsProperty) ?: "runtime"
//                )
//            } ?: ArrayList()
//        }

//        val handler = schema.contextHandle
//        if (!handler.supply(MavenVersionContext(desc.group, desc.artifact, desc.version!!))) return null
//        val pomResource by handler[schema.pom]



        val pom = loadMavenPom(pomOf(desc) ?: return null)

        val constantProperties: Map<String, String?> = LazyMap {
            when (it) {
                "project.version" -> pom.desc.version
                "project.parent.version" -> pom.parent?.desc?.version
                else -> null
            }
        }

        fun String.asIfProperty(): String {
            val match = propertyMatcher.matchEntire(this) ?: return this
            val name = match.groupValues[1]
            return pom.findProperty(name) ?: constantProperties[name]
            ?: throw IllegalArgumentException("Invalid property value: $this")
        }

        val dependencies = pom.dependencies

        val needed = dependencies.filter {
            when (it.scope) {
                "compile", "provided", "runtime", "import" -> true
                else -> false
            }
        }.map {
            MavenDescriptor(
                it.groupId.asIfProperty(),
                it.artifactId.asIfProperty(),
                it.version?.asIfProperty()
            )
        }
//        + dependencies.filter { it.scope == "import" }.map {
////            fun validatePom(ms: MavenSchema): SafeResource {
////                val handle = ms.contextHandle
////                handle.supply(MavenVersionContext(it.groupId, it.artifactId, it.version!!))
////                val importedPom by handle[ms.pom]
////
////                return importedPom
////            }
//
////            xml.readValue<PomData>(validatePom(schema).open())
//        }.flatMap(::loadPomDependencies)

//        val repositories = (pom.repositories ?: HashSet())
//            .map(PomRepository::url)
//            .let {
//                if (!it.contains(settings.url)) it + (settings.url
//                    ?: mavenCentral /* TODO Doing the null check then if null putting maven central is not a great way of doing this */) else it
//            }.filterNot { it == mavenCentral }
//            .mapTo(HashSet()) {
//                RepositorySettings(
//                    RepositoryType.MAVEN,
//                    it
//                )
//            } + RepositorySettings(RepositoryType.MAVEN_CENTRAL, null)


        val contains = HashSet<String>()

        val repositories = pom.repositories.filter(contains::add).map { r -> RepositorySettings(MAVEN, URL(r).toExternalForm()) }

        return Dependency(
            jarOf(desc),
            needed.mapTo(HashSet()) {
                Dependency.Transitive(repositories, it)
            },
            desc
        )
    }

    override fun loadDescription(dep: String): MavenDescriptor? =
        dep.split(':').takeIf { it.size == 3 || it.size == 2 }?.let { MavenDescriptor(it[0], it[1], it.getOrNull(2)) }
}