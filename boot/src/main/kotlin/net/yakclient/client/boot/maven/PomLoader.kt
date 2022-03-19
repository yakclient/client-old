@file:JvmName("PomLoader")

package net.yakclient.client.boot.maven

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.YakClient
import net.yakclient.client.util.resource.SafeResource
import net.yakclient.client.util.toResource
import java.nio.file.Paths

private val mapper = XmlMapper().registerModule(KotlinModule())

internal fun loadMavenPom(pom: SafeResource): Pom = loadPom(pom) { data ->
    val parent = data.parent ?: return@loadPom TheSuperPom
    val schemas = listOf(mavenCentralSchema) + (data.repositories?.map(PomRepository::url)?.map(::RemoteMavenSchema)
        ?: listOf<MavenSchema>())
    loadMavenPom(schemas.firstNotNullOfOrNull { schema ->
        val handle = schema.contextHandle
        if (handle.supply(
                MavenVersionContext(
                    parent.groupId,
                    parent.artifactId,
                    parent.version
                )
            )
        ) handle.getValue(schema.pom)
        else null
    } ?: throw IllegalStateException("Failed to load parent: '${parent.groupId}:${parent.artifactId}:${parent.version}'"))
}

internal fun loadPom(pom: SafeResource, parentProvider: (PomData) -> Pom?): Pom =
    loadPomData(pom).let { createPom(it, it.let(parentProvider)) }

private fun createPom(data: PomData, parent: Pom?) = ChildPom(data, parent)

private fun loadPomData(pom: SafeResource): PomData {
    val p = mapper.readValue<PomData>(pom.open())
    return p
}

