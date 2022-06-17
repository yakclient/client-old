package net.yakclient.client.boot.maven.pom

import net.yakclient.client.boot.maven.MavenPropertySource
import net.yakclient.common.util.LazyMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

private val propertyMatcher = Regex("^\\$\\{(.*)}$")

internal fun String.matchAsProperty(): String? = propertyMatcher.matchEntire(this)?.groupValues?.get(1)

internal class PropertyReplacer(
    private val sources: List<MavenPropertySource>
) {
    fun String.ifAsProperty(): String =
        matchAsProperty()?.let { p ->
            sources.firstNotNullOfOrNull { it.properties[p] }
        }?.ifAsProperty() ?: this

    fun <T : Any> replaceProperties(any: T): T {
        val kClass: KClass<T> = any::class as KClass<T>

        check(kClass.isData)

        val primaryConstructor = checkNotNull(kClass.primaryConstructor)

        val arguments = primaryConstructor.parameters
            .map { prop -> kClass.memberProperties.find { it.name == prop.name }!! }
            .map { it.get(any) }
            .map { if (it is String) it.ifAsProperty() else it }

        return primaryConstructor.call(*arguments.toTypedArray())
    }

    companion object {
        fun <R> of(vararg sources: MavenPropertySource, block: PropertyReplacer.() -> R): R =
            PropertyReplacer(sources.toList()).run(block)

        fun <R> of(
            data: PomData,
            parents: List<PomData>,
            vararg others: MavenPropertySource,
            block: PropertyReplacer.() -> R
        ): R =
            PropertyReplacer(
                listOf(
                    PropertyInterpolationSource(data),
                    DefaultPropertySource(data, parents)
                ) + others
            ).run(block)
    }
}

private class PropertyInterpolationSource(
    private val data: PomData
) : MavenPropertySource {
    override val properties: Map<String, String> by data::properties
}

private class DefaultPropertySource(
    private val data: PomData,
    private val parents: List<PomData>
) : MavenPropertySource {
    override val properties: Map<String, String> = LazyMap {
        when (it) {
            "project.artifactId" -> data.artifactId
            "project.version" -> data.version
            "project.groupId" -> data.groupId
            "project.parent.artifactId" -> parents.first().artifactId
            "project.parent.version" -> parents.firstNotNullOf { it.version }
            else -> null
        }
    }
}

internal fun PropertyReplacer.doInterpolation(
    data: PomData
): PomData {
    val dependencyManagement = data.dependencyManagement.dependencies
        .mapTo(HashSet(), ::replaceProperties)
        .let(::DependencyManagement)
    val dependencies = data.dependencies.mapTo(HashSet(), ::replaceProperties)
    val repositories = data.repositories.map(::replaceProperties)

    val extensions = data.build.extensions.map(::replaceProperties)
    val plugins = data.build.plugins.map(::replaceProperties)
    val pluginManagement =
        data.build.pluginManagement.plugins.map(::replaceProperties).let(::PomPluginManagement)

    val build = PomBuild(extensions, plugins, pluginManagement)

    val packaging = data.packaging.ifAsProperty()

    return PomData(
        data.groupId,
        data.artifactId,
        data.version,
        data.properties,
        data.parent,
        dependencyManagement,
        dependencies,
        repositories,
        build,
        packaging
    )
}