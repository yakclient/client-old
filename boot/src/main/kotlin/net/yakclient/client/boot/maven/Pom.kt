package net.yakclient.client.boot.maven

import net.yakclient.client.boot.repository.RepositorySettings

//
//internal interface Pom {
//    val parent: Pom?
//    val desc: MavenDescriptor
//    val properties: Map<String, String>
//    val repositories: List<RepositorySettings>
//    val dependencies: Set<PomDependency>
//    val managedDependencies: Set<PomDependency>
//    val billsOfMaterials: List<Pom>
//    val packaging: String
//
//    fun findProperty(name: String): String?
//}
//
//internal data class ChildPom(
//    private val data: PomData,
//    override val parent: Pom,
//    override val billsOfMaterials: List<ChildPom>
//) : Pom {
//    override val desc: MavenDescriptor by lazy {
//        val (g, a, v) = data
//
//        MavenDescriptor(
//            g ?: parent.desc.group,
//            a,
//            v ?: parent.desc.version
//        )
//    }
//    override val properties: Map<String, String> by lazy {
//        parent.properties + data.properties
//    }
//    override val repositories: List<RepositorySettings> by lazy {
//        parent.repositories + data.repositories.map {
//            RepositorySettings(
//                MAVEN,
//                mapOf(URL_OPTION_NAME to it.url, LAYOUT_OPTION_NAME to (it.layout ?: DEFAULT_MAVEN_LAYOUT))
//            )
//        }
//    }
//    override val dependencies: Set<PomDependency> by lazy {
//        fun ChildPom.findByOrNull(g: String, a: String): ManagedDependency? =
//            data.dependencyManagement.dependencies.find { it.groupId == g && it.artifactId == a }
//                ?: billsOfMaterials.firstNotNullOfOrNull { it.findByOrNull(g, a) }
//                ?: if (parent is ChildPom) parent.findByOrNull(g, a) else null
//
//        fun ChildPom.findBy(g: String, a: String): ManagedDependency = findByOrNull(g, a)
//            ?: throw IllegalStateException("Failed to find dependency('$g:$a') in dependency management! Pom was: '${desc.group}:${desc.artifact}:${desc.version}'")
//
//        parent.dependencies + data.dependencies.mapTo(HashSet()) { (g, a, v, s) ->
//            val groupId = g.matchAsProperty()?.let(::findProperty) ?: g
//            val artifactId = a.matchAsProperty()?.let(::findProperty) ?: a
//            PomDependency(
//                groupId,
//                artifactId,
//                v ?: findBy(groupId, artifactId).version,
//                s ?: findByOrNull(groupId, artifactId)?.scope ?: "compile"
//            )
//        }
//    }
//    override val managedDependencies: Set<PomDependency> =
//        data.dependencyManagement.dependencies.mapTo(HashSet()) { (g, a, v, s) ->
//            PomDependency(g, a, v, s ?: "compile")
//        } + parent.managedDependencies + billsOfMaterials.flatMap(Pom::managedDependencies)
//    override val packaging: String = data.packaging
//
//    override fun findProperty(name: String): String? = properties[name] ?: parent.findProperty(name)
//}
//
//internal object TheSuperPom : Pom {
//    override val parent: Pom? = null
//    override val desc: MavenDescriptor
//        get() = throw IllegalStateException("Cannot query the descriptor of the super pom! It should already be known in the pom hierarchy.")
//    override val properties: Map<String, String> = mapOf()
//    override val repositories: List<RepositorySettings> = listOf(RepositorySettings(MAVEN_CENTRAL))
//    override val dependencies: Set<PomDependency> = setOf()
//    override val managedDependencies: Set<PomDependency> = setOf()
//    override val billsOfMaterials: List<Pom> = listOf()
//    override val packaging: String = "pom"
//
//    override fun findProperty(name: String): String? = null
//}