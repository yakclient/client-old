package net.yakclient.client.boot.test.dep

import net.yakclient.client.boot.InitScope
import net.yakclient.client.boot.dependency.DependencyGraph
import net.yakclient.client.boot.init
import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.common.util.child
import net.yakclient.common.util.parent
import net.yakclient.common.util.workingDir
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference
import java.nio.file.Path
import kotlin.test.Test

class TestDependencyGraph {
    @Test
    fun `Test dependency graph`() {
        init(workingDir().parent("client").child("workingDir").toPath())

        val ref = DependencyGraph.ofRepository(RepositorySettings(MAVEN_CENTRAL))
            .load("com.google.guava:guava:31.0.1-jre")

        println(ref)
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
        val modules =
            (1..100).toList().map { parentLayer.defineModules(config) {object :ClassLoader() {} } }
                .map { it.modules().first() }

        println(modules)
    }

    @Test
    fun `Load dependency from graph`() {
        init(workingDir().parent("client").child("workingDir").toPath(), InitScope.DEVELOPMENT)

        val ref = DependencyGraph.ofRepository(
            RepositorySettings(
                MAVEN, options = mapOf(
                    LAYOUT_OPTION_NAME to SNAPSHOT_MAVEN_LAYOUT,
                    URL_OPTION_NAME to "https://repo.heartpattern.io/repository/maven-public/"
                )
            )
        ).load("io.heartpattern:mcremapper:2.0.6-SNAPSHOT")

        println(ref)
    }

}

