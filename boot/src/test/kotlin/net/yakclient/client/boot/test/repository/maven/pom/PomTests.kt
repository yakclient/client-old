package net.yakclient.client.boot.test.repository.maven.pom

import net.yakclient.client.boot.InitScope
import net.yakclient.client.boot.init
import net.yakclient.client.boot.internal.CentralMavenLayout
import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.maven.parseData
import net.yakclient.client.boot.maven.pom.*
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.child
import net.yakclient.client.util.parent
import net.yakclient.client.util.workingDir
import kotlin.test.BeforeTest
import kotlin.test.Test

class PomTests {
    @BeforeTest
    fun init() {
        init(workingDir().parent("client").child("workingDir").toPath(), InitScope.TEST)
    }

    @Test
    fun `Test property matching Regex`() {
        val regex = Regex("^\\$\\{(.*)}$")

        assert(!regex.matches("asdf"))
        assert(regex.matches("\${asdf}"))

        val r = checkNotNull(regex.find("\${test.property}"))
        println(r.groupValues[1])
    }

    @Test
    fun `Test Pom Loading`() {
        val repo = RepositoryFactory.create(RepositorySettings(type = MAVEN_CENTRAL)) as MavenRepositoryHandler

        val loadMavenPom = repo.parsePom(
            CentralMavenLayout.artifactOf(
                "io.netty",
                "netty-handler",
                "4.1.77.Final",
                null,
                "pom"
            )!!
        )
        println(loadMavenPom)
    }

    @Test
    fun `Test Parent Resolving Stage`() {
        val repo = RepositoryFactory.create(RepositorySettings(type = MAVEN_CENTRAL)) as MavenRepositoryHandler

        val stage = ParentResolutionStage()

        val data = parseData(CentralMavenLayout.artifactOf("com.google.guava", "guava", "31.0.1-jre", null, "pom")!!)

        println(stage.process(WrappedPomData(data, repo)))
    }

    @Test
    fun `Test Inheritance Assembly`() {
        val repo = RepositoryFactory.create(RepositorySettings(type = MAVEN_CENTRAL)) as MavenRepositoryHandler

        val parentResolutionStage = ParentResolutionStage()
        val inheritanceAssemblyStage = PomInheritanceAssemblyStage()

        val data = parseData(CentralMavenLayout.artifactOf("com.google.guava", "guava", "31.0.1-jre", null, "pom")!!)

        println(
            inheritanceAssemblyStage.process(
                parentResolutionStage.process(
                    WrappedPomData(
                        data, repo
                    )
                )
            )
        )
    }

    @Test
    fun `Test Interpolation`() {
        val repo = RepositoryFactory.create(RepositorySettings(type = MAVEN_CENTRAL)) as MavenRepositoryHandler

        val parentResolutionStage = ParentResolutionStage()
        val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
        val interpolationStage = PrimaryInterpolationStage()

        val data = parseData(CentralMavenLayout.artifactOf("com.google.guava", "guava", "31.0.1-jre", null, "pom")!!)

        println(
            interpolationStage.process(
                inheritanceAssemblyStage.process(
                    parentResolutionStage.process(
                        WrappedPomData(
                            data, repo
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Test Plugin management injection`() {
        val repo = RepositoryFactory.create(RepositorySettings(type = MAVEN_CENTRAL)) as MavenRepositoryHandler

        val parentResolutionStage = ParentResolutionStage()
        val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
        val interpolationStage = PrimaryInterpolationStage()
        val pluginManagementInjectionStage = PluginManagementInjectionStage()

        val data = parseData(CentralMavenLayout.artifactOf("com.google.guava", "guava", "31.0.1-jre", null, "pom")!!)

        val result = WrappedPomData(
            data, repo
        ).let(parentResolutionStage::process).let(inheritanceAssemblyStage::process).let(interpolationStage::process)
            .let(pluginManagementInjectionStage::process)

        println(result)
    }

    @Test
    fun `Test Plugin Loading`() {
        val repo = RepositoryFactory.create(RepositorySettings(type = MAVEN_CENTRAL)) as MavenRepositoryHandler

        val parentResolutionStage = ParentResolutionStage()
        val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
        val interpolationStage = PrimaryInterpolationStage()
        val pluginManagementInjectionStage = PluginManagementInjectionStage()
        val pluginLoadingStage = PluginLoadingStage()

        val data = parseData(CentralMavenLayout.artifactOf("io.netty", "netty-handler", "4.1.77.Final", null, "pom")!!)

        val result = WrappedPomData(
            data, repo
        ).let(parentResolutionStage::process).let(inheritanceAssemblyStage::process).let(interpolationStage::process)
            .let(pluginManagementInjectionStage::process).let(pluginLoadingStage::process)

        println(result.plugins)
    }

    @Test
    fun `Test Secondary Interpolation`() {
        val repo = RepositoryFactory.create(RepositorySettings(type = MAVEN_CENTRAL)) as MavenRepositoryHandler

        val parentResolutionStage = ParentResolutionStage()
        val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
        val interpolationStage = PrimaryInterpolationStage()
        val pluginManagementInjectionStage = PluginManagementInjectionStage()
        val pluginLoadingStage = PluginLoadingStage()
        val secondaryInterpolationStage = SecondaryInterpolationStage()

        val data = parseData(CentralMavenLayout.artifactOf("io.netty", "netty-handler", "4.1.77.Final", null, "pom")!!)

        val result = WrappedPomData(
            data, repo
        ).let(parentResolutionStage::process).let(inheritanceAssemblyStage::process).let(interpolationStage::process)
            .let(pluginManagementInjectionStage::process).let(pluginLoadingStage::process)
            .let(secondaryInterpolationStage::process)

        println(result.data)
    }

    @Test
    fun `Test dependency management injection`() {
        val repo = RepositoryFactory.create(RepositorySettings(type = MAVEN_CENTRAL)) as MavenRepositoryHandler

        val parentResolutionStage = ParentResolutionStage()
        val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
        val interpolationStage = PrimaryInterpolationStage()
        val pluginManagementInjectionStage = PluginManagementInjectionStage()
        val pluginLoadingStage = PluginLoadingStage()
        val secondaryInterpolationStage = SecondaryInterpolationStage()
        val dependencyManagementInjector = DependencyManagementInjectionStage()

        val data = parseData(CentralMavenLayout.artifactOf("io.netty", "netty-handler", "4.1.77.Final", null, "pom")!!)

        val result = WrappedPomData(
            data, repo
        ).let(parentResolutionStage::process).let(inheritanceAssemblyStage::process).let(interpolationStage::process)
            .let(pluginManagementInjectionStage::process).let(pluginLoadingStage::process)
            .let(secondaryInterpolationStage::process).let(dependencyManagementInjector::process)

        println(data.dependencies)
        println(result.data.dependencies)
    }

    @Test
    fun `Test pom finalizing`() {
        val repo = RepositoryFactory.create(RepositorySettings(type = MAVEN_CENTRAL)) as MavenRepositoryHandler

        val parentResolutionStage = ParentResolutionStage()
        val inheritanceAssemblyStage = PomInheritanceAssemblyStage()
        val interpolationStage = PrimaryInterpolationStage()
        val pluginManagementInjectionStage = PluginManagementInjectionStage()
        val pluginLoadingStage = PluginLoadingStage()
        val secondaryInterpolationStage = SecondaryInterpolationStage()
        val dependencyManagementInjector = DependencyManagementInjectionStage()
        val pomFinalizer = PomFinalizingStage()

        val data = parseData(CentralMavenLayout.artifactOf("io.netty", "netty-handler", "4.1.77.Final", null, "pom")!!)

        val result = WrappedPomData(
            data, repo
        ).let(parentResolutionStage::process).let(inheritanceAssemblyStage::process).let(interpolationStage::process)
            .let(pluginManagementInjectionStage::process).let(pluginLoadingStage::process)
            .let(secondaryInterpolationStage::process).let(dependencyManagementInjector::process)
            .let(pomFinalizer::process)

        println(result)
    }
}


