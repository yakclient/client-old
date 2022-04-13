package net.yakclient.client.boot

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.github.config4k.registerCustomType
import kotlinx.cli.ArgParser
import kotlinx.cli.required
import net.yakclient.client.boot.YakClient.dependencyResolver
import net.yakclient.client.boot.archive.ArchiveHandle
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.boot.dependency.ArchiveDependencyResolver
import net.yakclient.client.boot.dependency.DependencyGraph
import net.yakclient.client.boot.dependency.DependencyResolutionFallBack
import net.yakclient.client.boot.dependency.DependencyResolver
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.extension.ExtensionLoader
import net.yakclient.client.boot.internal.jpm.JpmHandle
import net.yakclient.client.boot.internal.jpm.ResolvedJpm
import net.yakclient.client.boot.maven.MAVEN_CENTRAL
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.*
import net.yakclient.client.util.resource.SafeResource
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
                }?.let { ResolvedJpm(it) }

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
        (run.exceptionOrNull()!!).printStackTrace()
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
        ResolvedJpm(
            YakClient::class.java.module,
        ),
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
        dl load "com.typesafe:config:1.4.1"
        dl load "com.fasterxml.jackson.module:jackson-module-kotlin:2.12.6"
        dl load "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.6"
        dl load "org.jetbrains.kotlin:kotlin-stdlib-common:2.0.0-beta-1"
//        dl load "org.apache.httpcomponents:httpclient:4.5.13"
        dl load "io.ktor:ktor-client-cio:2.0.0"


//      dl load "io.ktor:ktor-client-core-jvm:1.3.2-1.4-M2"

//       val client = HttpClient(CIO) {
//           expectSuccess = false
//       }
//
////        val req = client.get("https://ktor.io/")
//        println(client.engine)

    }
}

