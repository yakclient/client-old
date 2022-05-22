@file:JvmName("PomLoader")

package net.yakclient.client.boot.maven

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.internal.CentralMavenLayout
import net.yakclient.client.boot.maven.layout.InvalidMavenLayoutException
import net.yakclient.client.boot.maven.layout.MavenLayoutFactory
import net.yakclient.client.boot.maven.layout.MavenRepositoryLayout
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.common.util.filterDuplicates
import net.yakclient.common.util.resource.SafeResource
import net.yakclient.common.util.runCatching
//
//private val mapper = XmlMapper().registerModule(KotlinModule())
//private const val BOM_IMPORT_SCOPE = "import"
//
//internal fun MavenRepositoryLayout.loadMavenPom(pom: SafeResource): Pom =
//    loadMavenPom(pom, this)
//
//internal fun loadMavenPom(pom: SafeResource, thisLayout: MavenRepositoryLayout): Pom {
//    val data = loadPomData(pom)
//
//    val layouts = listOf(thisLayout, CentralMavenLayout) + data.repositories.map {
//        RepositorySettings(
//            it.url, options = mapOf(LAYOUT_OPTION_NAME to (it.layout ?: DEFAULT_MAVEN_LAYOUT))
//        )
//    }.map(MavenLayoutFactory::createLayout).filterDuplicates()
//
//    fun loadExternalPom(g: String, a: String, v: String): Pom {
//        val resource = layouts.firstNotNullOfOrNull { layout ->
//            runCatching(InvalidMavenLayoutException::class) { layout.pomOf(g, a, v) }?.let { it to layout }
//        } ?: throw IllegalStateException("Failed to load external pom(bom or parent): '$g:$a:$v' for pom: '${pom.uri}'.")
//
//        return loadMavenPom(resource.first, resource.second)
//    }
//
//    val parent = data.parent?.let { (g, a, v) -> loadExternalPom(g, a, v) } ?: TheSuperPom
//
//    fun String.ifAsProperty() : String = matchAsProperty()?.let { data.properties[it] ?: parent.findProperty(it) ?: when(it) {
//        "project.version" -> data.version ?: parent.desc.version
//        "project.parent.version" -> parent.desc.version
//        else -> throw IllegalArgumentException("Failed to find property: $this in pom of : ${pom.uri}")
//    } }?.ifAsProperty() ?: this
//
//    val boms = data.dependencyManagement.dependencies.filter { it.scope == BOM_IMPORT_SCOPE }.map {
//        loadExternalPom(it.groupId.ifAsProperty(), it.artifactId.ifAsProperty(), it.version.ifAsProperty())
//    }
//
//    return ChildPom(data, parent, boms as List<ChildPom>)
//}
//
////internal fun loadMavenPom(pom: SafeResource, thisLayout: MavenRepositoryLayout): Pom = loadPom(pom) { data ->
////    val (groupId, artifactId, version) = data.parent ?: return@loadPom TheSuperPom
////
////    val layouts = listOf(thisLayout, CentralMavenLayout) + data.repositories.map {
////        RepositorySettings(
////            it.url, options = mapOf(LAYOUT_OPTION_NAME to (it.layout ?: DEFAULT_MAVEN_LAYOUT))
////        )
////    }.map(MavenLayoutFactory::createLayout).filterDuplicatesBy { it.settings.url }
////
////    loadMavenPom(layouts.firstNotNullOfOrNull { layout ->
////        runCatching(InvalidMavenLayoutException::class) { layout.pomOf(groupId, artifactId, version) }
////    } ?: throw IllegalStateException("Failed to load parent: '${groupId}:${artifactId}:${version}'"), thisLayout)
////}
//
////internal fun loadPom(pom: SafeResource, parentProvider: (PomData) -> Pom): Pom =
////    loadPomData(pom).let { createPom(it, it.let(parentProvider)) }
////
////private fun createPom(data: PomData, parent: Pom) = ChildPom(data, parent)
//
//private fun loadPomData(pom: SafeResource): PomData = mapper.readValue(pom.open())

