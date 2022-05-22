package net.yakclient.client.boot

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.github.config4k.registerCustomType
import kotlinx.cli.ArgParser
import kotlinx.cli.required
import net.yakclient.archives.ArchiveHandle
import net.yakclient.archives.ResolvedArchive
import net.yakclient.archives.impl.jpm.JpmHandle
import net.yakclient.archives.impl.jpm.ResolvedJpm
import net.yakclient.client.boot.YakClient.dependencyResolver
import net.yakclient.client.boot.dependency.ArchiveDependencyResolver
import net.yakclient.client.boot.dependency.DependencyGraph
import net.yakclient.client.boot.dependency.DependencyResolutionFallBack
import net.yakclient.client.boot.dependency.DependencyResolver
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.extension.ExtensionLoader
import net.yakclient.client.boot.maven.MAVEN_CENTRAL
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.PathArgument
import net.yakclient.client.util.UriCustomType
import net.yakclient.common.util.*
import net.yakclient.common.util.resource.SafeResource
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Level
import kotlin.system.exitProcess

public object YakClient : Extension() {
    internal var innited: Boolean = false
    public var settings: BootSettings by immutableLateInit()
    public var yakDir: Path by immutableLateInit()
    public val dependencyResolver: DependencyResolver =
        object : DependencyResolutionFallBack(ArchiveDependencyResolver()) {
            override fun resolve(ref: ArchiveHandle, dependants: List<ResolvedArchive>): ResolvedArchive? {
                fun moduleByName(name: String): ResolvedArchive? = ModuleLayer.boot().modules().find {
                    it.name == name
                }?.let(::ResolvedJpm)
                return if (ref is JpmHandle) when (ref.descriptor().name()) {
                    "javaee.api" -> moduleByName("java.xml")
                    else -> moduleByName(ref.descriptor().name())
                } else null
            }
        }


    public fun exit(e: Exception, extra: String = "A critical error has occurred"): Nothing {
        logger.log(Level.SEVERE, extra)
        logger.log(Level.SEVERE, "YakClient had to quit unexpectedly because : ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }

    internal fun loadResource(name: String): SafeResource? =
        YakClient::class.java.getResource(name)?.let { Paths.get(it.toURI()) }?.toResource()
}

private const val SETTINGS_NAME = "settings.conf"

public fun main(args: Array<String>) {
    val parser = ArgParser("yakclient")

    val yakDirectory by parser.option(PathArgument, "yakdirectory", "d").required()

    parser.parse(args)

    init(yakDirectory)

    val run = runCatching {
        ExtensionLoader.load(YakClient.settings.apiLocation, YakClient).onLoad()
    }

    if (run.isFailure) {
        YakClient.logger.log(Level.INFO, "Error occurred, Exiting gracefully")
        run.exceptionOrNull()!!.printStackTrace(System.err)
    } else YakClient.logger.log(Level.INFO, "Successfully Quit")

}

public enum class InitScope {
    PRODUCTION,
    DEVELOPMENT,
    TEST
}

public fun init(yakDir: Path, scope: InitScope = InitScope.DEVELOPMENT) {
    if (YakClient.innited) return
    YakClient.innited = true

    registerCustomType(UriCustomType())

    YakClient.yakDir = yakDir

    YakClient.settings = ConfigFactory.parseFile((yakDir resolve SETTINGS_NAME).toFile()).extract("boot")

    // Clear temp directory
    YakClient.settings.tempPath.deleteAll()

    YakClient.init(
        ResolvedJpm(YakClient::class.java.module),
        YakClient.settings,
        null
    )

    val dl = DependencyGraph.DependencyLoader(
        RepositoryFactory.create(
            RepositorySettings(MAVEN_CENTRAL)
        ),
        dependencyResolver
    )

    if (scope.equalsAny(InitScope.PRODUCTION, InitScope.DEVELOPMENT)) {
        dl load "org.jetbrains.kotlinx:kotlinx-cli-jvm:0.3.4"
        dl load "org.jetbrains.kotlinx:kotlinx-coroutines-core:2.0.0-beta-1"
        dl load "org.jetbrains.kotlin:kotlin-reflect:1.6.0"
        dl load "io.github.config4k:config4k:0.4.2"
//        dl load "com.typesafe:config:1.4.1"
        dl load "com.fasterxml.jackson.module:jackson-module-kotlin:2.12.6"
        dl load "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.6"
        dl load "org.jetbrains.kotlin:kotlin-stdlib-common:2.0.0-beta-1"
        dl load "io.ktor:ktor-client-cio:2.0.0"
        dl load "net.yakclient:archives:1.0-SNAPSHOT"
    }
}

