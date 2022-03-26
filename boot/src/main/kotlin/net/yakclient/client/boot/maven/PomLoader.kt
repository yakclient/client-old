@file:JvmName("PomLoader")

package net.yakclient.client.boot.maven

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.internal.CentralMavenLayout
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.resource.SafeResource
import net.yakclient.client.util.runCatching

private val mapper = XmlMapper().registerModule(KotlinModule())

internal fun loadMavenPom(pom: SafeResource): Pom = loadPom(pom) { data ->
    val parent = data.parent ?: return@loadPom TheSuperPom
    val schemas = listOf(CentralMavenLayout) + (data.repositories?.map {
        RepositorySettings(
            it.url,
            options = mapOf(LAYOUT_OPTION_NAME to (it.layout ?: DEFAULT_MAVEN_LAYOUT))
        )
    }?.map(MavenLayoutFactory::createLayout) ?: listOf())
    loadMavenPom(schemas.firstNotNullOfOrNull { layout ->
        runCatching(InvalidMavenLayoutException::class) {
            layout.pomOf(
                parent.groupId,
                parent.artifactId,
                parent.version
            )
        }
    }
        ?: throw IllegalStateException("Failed to load parent: '${parent.groupId}:${parent.artifactId}:${parent.version}'"))
}

internal fun loadPom(pom: SafeResource, parentProvider: (PomData) -> Pom?): Pom =
    loadPomData(pom).let { createPom(it, it.let(parentProvider)) }

private fun createPom(data: PomData, parent: Pom?) = ChildPom(data, parent)

private fun loadPomData(pom: SafeResource): PomData {
    val p = mapper.readValue<PomData>(pom.open())
    return p
}

