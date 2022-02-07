package net.yakclient.client.boot.internal.maven

import net.yakclient.client.boot.internal.maven.property.*
import net.yakclient.client.boot.internal.maven.property.PomPropertyProvider
import net.yakclient.client.boot.dep.Dependency
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType
import net.yakclient.client.boot.schema.validate
import net.yakclient.client.util.get
import net.yakclient.client.util.isReachable
import net.yakclient.client.util.openStream
import net.yakclient.client.util.valueOf
import org.w3c.dom.Element
import java.net.URI
import java.util.logging.Level
import java.util.logging.Logger
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

private const val DEFAULT_SCOPE = "runtime"

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
    private val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance().also {
        it.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    }


    private val propertyProviders = listOf(
        PomPropertyProvider(factory, schema),
        PomVersionProvider(factory, schema)
    )

    private fun loadProperty(doc: Element, property: String) =
        propertyProviders.firstNotNullOfOrNull { it.provide(doc, property) }

    // TODO Make this nicer, For example something you can add to the DependencyGraph$DependencyLoader that allows a backup repository?
    override fun find(desc: MavenDescriptor): Dependency? =
        findInternal(desc) ?: if (settings.type != RepositoryType.MAVEN_CENTRAL) (RepositoryFactory.create(
            RepositorySettings(
                RepositoryType.MAVEN_CENTRAL,
                null
            )
        ) as MavenRepositoryHandler).find(desc) else null

    private fun substituteVersion(desc: MavenDescriptor): MavenDescriptor? {
        if (desc.version != null) return desc

        return MavenDescriptor(desc.group, desc.artifact, schema.validate(MavenSchemeContext(desc)) {
            val meta = get(schema.meta)
            val docElem = factory.newDocumentBuilder().parse(meta.openStream()).documentElement

            docElem.valueOf("version")
                ?: docElem["versioning"].first().valueOf("release")
                ?: return@validate null
        })
    }


    private fun findInternal(desc: MavenDescriptor): Dependency? {
        return schema.validate(MavenSchemeContext(substituteVersion(desc) ?: return null)) {
            logger.log(Level.FINEST, "Loading maven dependency: '$desc'")

            fun loadPomDependencies(pom: URI): List<MavenDependency> {
                val doc = factory.newDocumentBuilder().parse(pom.openStream()).documentElement

                return doc["dependencies"].firstOrNull()?.get("dependency")?.map {
                    val depGroup = it.valueOf("groupId")!!
                    val depArtifact = it.valueOf("artifactId")!!

                    fun loadProperty(name: String): String? = loadProperty(doc, name)

                    fun loadIfAsProperty(value: String): String =
//                        if (value == null) null
                        if (value.startsWith("\${") && value.endsWith("}")) {
                            loadIfAsProperty(
                                loadProperty(value.substring(2, value.length - 1))
                                    ?: throw IllegalArgumentException("Expected to find property for value: $value in artifact: '$depGroup:$depArtifact' but instead found null")
                            )
                        } else value

                    MavenDependency(
                        loadIfAsProperty(depGroup),
                        loadIfAsProperty(depArtifact),
                        it.valueOf("version")?.let(::loadIfAsProperty),
                        it.valueOf("scope")?.let(::loadIfAsProperty) ?: DEFAULT_SCOPE
                    )
                } ?: ArrayList()
            }

            val pom = get(schema.pom) ?: return@validate null
            if (!pom.toURL().isReachable()) return@validate null
            val dependencies = loadPomDependencies(pom)

            val needed = dependencies.filter {
                when (it.scope) {
                    "compile", "provided", "runtime" -> true
                    else -> false
                }
            } + dependencies.filter { it.scope == "import" }.map {
                fun validatePom(ms: MavenSchema): URI? = ms.validate(MavenSchemeContext(it.toDescriptor()))?.get(ms.pom)
                // TODO Find a better solution to this
                validatePom(schema) ?: (if (settings.type != RepositoryType.MAVEN_CENTRAL) validatePom(MavenCentralSchema) else null) ?: throw IllegalStateException("Failed to find imported maven pom dependency type: $it")
            }.flatMap(::loadPomDependencies)

            Dependency(
                get(schema.jar) ?: return@validate null,
                needed.map { MavenDescriptor(it.group, it.artifact, it.version) },
                desc
            )
        }
    }

    override fun loadDescription(dep: String): MavenDescriptor? =
        dep.split(':').takeIf { it.size == 3 || it.size == 2 }?.let { MavenDescriptor(it[0], it[1], it.getOrNull(2)) }
}