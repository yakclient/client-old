package net.yakclient.client.boot.test.dep

import net.yakclient.client.boot.dependency.DependencyGraph
import net.yakclient.client.boot.init
import net.yakclient.client.boot.maven.MAVEN_CENTRAL
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.child
import net.yakclient.client.util.parent
import net.yakclient.client.util.workingDir
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference
import java.nio.file.Path
import kotlin.test.Test

class TestDependencyGraph {
    @Test
    fun `Test dependency graph`() {
        init(workingDir().parent("client").child("workingDir").toPath())

        val ref = DependencyGraph.ofRepository(RepositorySettings(MAVEN_CENTRAL, null))
            .load("org.jetbrains:annotations:13.0")

        println(ref)
    }

    @Test
    fun `Test local dependency loading`() {
        init(workingDir().parent("client").child("workingDir").toPath())

        val ref = DependencyGraph.ofRepository(RepositorySettings(MAVEN_CENTRAL, null))
            .load("com.google.guava:guava:31.0.1-jre")

        println(ref)
//        println(ref.classloader.loadClass("com.google.common.annotations.Beta"))
    }

    @Test
    fun `Test loading different module versions`() {
        val parentLayer = ModuleLayer.boot()

        val finder =
            ModuleFinder.of(Path.of("/Users/durgan/IdeaProjects/yakclient/client/workingDir/cache/lib/jna-5.8.0.jar"))
        val config = parentLayer.configuration().resolve(
            finder,
            ModuleFinder.of(),
            finder.findAll().map(ModuleReference::descriptor).map(ModuleDescriptor::name)
        )
        val modules = listOf(1, 2).map { parentLayer.defineModulesWithOneLoader(config, ClassLoader.getSystemClassLoader()) }.map { it.modules().first() }
        println(modules)
    }


}

