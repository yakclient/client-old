package net.yakclient.client.boot.maven

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.maven.pom.*
import net.yakclient.client.boot.maven.pom.DependencyManagementInjectionStage
import net.yakclient.client.boot.maven.pom.ParentResolutionStage
import net.yakclient.client.boot.maven.pom.PluginLoadingStage
import net.yakclient.client.boot.maven.pom.PluginManagementInjectionStage
import net.yakclient.client.boot.maven.pom.PomFinalizingStage
import net.yakclient.client.boot.maven.pom.PomInheritanceAssemblyStage
import net.yakclient.client.boot.maven.pom.PrimaryInterpolationStage
import net.yakclient.client.boot.maven.pom.SecondaryInterpolationStage
import net.yakclient.common.util.resource.SafeResource

private val mapper = XmlMapper().registerModule(KotlinModule())

//private fun String.matchAsProperty(): String? = propertyMatcher.matchEntire(this)?.groupValues?.get(1)

private val parentResolutionStage = ParentResolutionStage()
private val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
private val primaryInterpolationStage = PrimaryInterpolationStage()
private val pluginManagementInjectionStage = PluginManagementInjectionStage()
private val pluginLoadingStage = PluginLoadingStage()
private val secondaryInterpolationStage = SecondaryInterpolationStage()
private val dependencyManagementInjector = DependencyManagementInjectionStage()
private val pomFinalizingStage = PomFinalizingStage()

public fun MavenRepositoryHandler.parsePom(resource: SafeResource): FinalizedPom = parsePom(parseData(resource))

public fun MavenRepositoryHandler.parsePom(data: PomData): FinalizedPom = WrappedPomData(data, this)
    .let(parentResolutionStage::process)
    .let(inheritanceAssemblyStage::process)
    .let(primaryInterpolationStage::process)
    .let(pluginManagementInjectionStage::process)
    .let(pluginLoadingStage::process)
    .let(secondaryInterpolationStage::process)
    .let(dependencyManagementInjector::process)
    .let(pomFinalizingStage::process)

public fun parseData(resource: SafeResource): PomData = mapper.readValue(resource.open())