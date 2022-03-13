package net.yakclient.client.boot.internal.maven

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.dependency.Dependency
import net.yakclient.client.boot.internal.maven.property.PomPropertyProvider
import net.yakclient.client.boot.internal.maven.property.PomVersionProvider
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType
import net.yakclient.client.util.resource.SafeResource
import java.util.logging.Level
import java.util.logging.Logger


internal class MavenRepositoryHandler(
    override val settings: RepositorySettings,
    private val schema: MavenSchema
) : RepositoryHandler<MavenDescriptor> {
    private val logger = Logger.getLogger(this::class.simpleName)

    //    private val repo = when (settings.type) {
//        RepositoryType.MAVEN -> settings.path
//            ?: throw IllegalArgumentException("No repository provided! Please include a path in your repository declaration(or declare MAVEN_CENTRAL as the type)")
//        RepositoryType.MAVEN_CENTRAL -> "https://repo.maven.apache.org/maven2"
//        else -> throw IllegalArgumentException("Unknown repo type: ${settings.type}")
//    }
//    private val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance().also {
//        it.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
//    }

    private val xml: ObjectMapper = XmlMapper().registerModule(KotlinModule())

    private val propertyProviders = listOf(
        PomPropertyProvider(xml, schema),
        PomVersionProvider(xml, schema)
    )

    private fun loadProperty(pom: Pom, property: String) =
        propertyProviders.firstNotNullOfOrNull { it.provide(pom, property) }

    // TODO Make this nicer, For example something you can add to the DependencyGraph$DependencyLoader that allows a backup repository?
    override fun find(desc: MavenDescriptor): Dependency? =
        findInternal(desc)
//            ?: if (settings.type != RepositoryType.MAVEN_CENTRAL) (RepositoryFactory.create(
//            RepositorySettings(
//                RepositoryType.MAVEN_CENTRAL,
//                null
//            )
//        ) as MavenRepositoryHandler).find(desc) else null

    private fun substituteVersion(desc: MavenDescriptor): MavenDescriptor? {
        if (desc.version != null) return desc

        val meta = schema.contextHandle.apply {
            supply(MavenArtifactContext(desc.group, desc.artifact))
        }.getValue(schema.meta)

        val tree = xml.readValue<Map<String, Any>>(meta.open())

        val version =
            (tree["version"] as? String) ?: (tree["versioning"] as Map<String, String>)["release"] ?: return null

        return MavenDescriptor(desc.group, desc.artifact, version)
    }


    private fun findInternal(_desc: MavenDescriptor): Dependency? {
        val desc = substituteVersion(_desc) ?: return null
//        return schema.validate(MavenArtifactContext(substituteVersion(desc) ?: return null)) {
        logger.log(Level.FINEST, "Loading maven dependency: '$desc'")


        fun loadPomDependencies(pom: Pom): List<MavenDependency> {
//                val pom = xml.readValue<Pom>(pr.open())

            return pom.dependencies?.map { dep ->
                val depGroup = dep.groupId
                val depArtifact = dep.artifactId

                fun loadProperty(name: String): String? = loadProperty(pom, name)

                fun loadIfAsProperty(value: String): String =
                    if (value.startsWith("\${") && value.endsWith("}")) {
                        loadIfAsProperty(
                            loadProperty(value.substring(2, value.length - 1))
                                ?: throw IllegalArgumentException("Expected to find property for value: $value in artifact: '$depGroup:$depArtifact' but instead found null")
                        )
                    } else value

                MavenDependency(
                    loadIfAsProperty(depGroup),
                    loadIfAsProperty(depArtifact),
                    dep.version?.let(::loadIfAsProperty) ?: substituteVersion(dep.toDescriptor())?.version,
                    dep.scope?.let(::loadIfAsProperty) ?: "runtime"
                )
            } ?: ArrayList()
        }

        val handler = schema.contextHandle
        handler.supply(MavenVersionContext(desc.group, desc.artifact, desc.version!!))
        val pomResource by handler[schema.pom]

        val pom = xml.readValue<Pom>(
            pomResource.open()
                ?: throw IllegalArgumentException("Failed to read pom of dependency: $desc in repo: '${settings.url ?: "MAVEN CENTRAL(url: '$mavenCentral')"}'")
        )
//            val pom = get(pom) ?: return@validate null
//            if (!pom.toURL().isReachable()) return@validate null
        val dependencies = loadPomDependencies(pom)

        val needed = dependencies.filter {
            when (it.scope) {
                "compile", "provided", "runtime" -> true
                else -> false
            }
        } + dependencies.filter { it.scope == "import" }.map {
            fun validatePom(ms: MavenSchema): SafeResource? {
                val handle = ms.contextHandle
                handle.supply(MavenVersionContext(it.groupId, it.artifactId, it.version!!))
                val importedPom by handle[ms.pom]

                return importedPom
            }

            xml.readValue<Pom>(
                (validatePom(schema)
                    ?: (if (settings.type != RepositoryType.MAVEN_CENTRAL) validatePom(
                        RemoteMavenSchema(mavenCentral)
                    ) else null)
                    ?: throw IllegalStateException("Failed to find imported maven pom dependency type: $it")).open()
            )
        }.flatMap(::loadPomDependencies)

        val repositories = (pom.repositories ?: HashSet())
            .map(PomRepository::url)
            .let {
                if (!it.contains(settings.url)) it + (settings.url
                    ?: mavenCentral /* TODO Doing the null check then if null putting maven central is not a great way of doing this */) else it
            }.filterNot { it == mavenCentral }
            .mapTo(HashSet()) { RepositorySettings(RepositoryType.MAVEN, it) } + RepositorySettings(RepositoryType.MAVEN_CENTRAL, null)

        val jar by handler[schema.pom]

        return Dependency(
            jar,
            needed.map {
                Dependency.Transitive(repositories, MavenDescriptor(it.groupId, it.artifactId, it.version))
            },
            desc
        )
//        }
    }

    override fun loadDescription(dep: String): MavenDescriptor? =
        dep.split(':').takeIf { it.size == 3 || it.size == 2 }?.let { MavenDescriptor(it[0], it[1], it.getOrNull(2)) }
}