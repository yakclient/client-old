package net.yakclient.client.boot

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.github.config4k.registerCustomType
import kotlinx.cli.ArgParser
import kotlinx.cli.required
import net.yakclient.archives.Archives
import net.yakclient.archives.JpmArchives
import net.yakclient.client.boot.container.ContainerLoader
import net.yakclient.client.boot.container.security.PrivilegeManager
import net.yakclient.client.boot.container.volume.*
import net.yakclient.client.boot.dependency.ArchiveDependencyResolver
import net.yakclient.client.boot.dependency.DependencyResolutionBid
import net.yakclient.client.boot.dependency.DependencyResolver
import net.yakclient.client.boot.dependency.orFallBackOn
import net.yakclient.client.boot.extension.Extension
import net.yakclient.client.boot.extension.ExtensionInfo
import net.yakclient.client.boot.extension.ExtensionLoader
import net.yakclient.client.boot.internal.InternalLayoutProvider
import net.yakclient.client.boot.internal.InternalRepoProvider
import net.yakclient.client.boot.internal.volume.RootVolume
import net.yakclient.client.boot.maven.layout.MavenLayoutFactory
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.util.PathArgument
import net.yakclient.client.util.UriCustomType
import net.yakclient.common.util.deleteAll
import net.yakclient.common.util.immutableLateInit
import net.yakclient.common.util.resolve
import java.nio.file.Path
import java.security.Policy
import java.util.logging.Level
import kotlin.system.exitProcess

public object YakClient : Extension() {
    internal var innited: Boolean = false
    public var settings: BootSettings by immutableLateInit()
    public var yakDir: Path by immutableLateInit()
    public val moduleResolver: DependencyResolutionBid = DependencyResolutionBid { ref, _ ->
        if (ref.name == null) return@DependencyResolutionBid null
        val name = when (ref.name) {
            "javaee.api" -> "java.xml"
            "slf4j.api" -> "org.slf4j"
            "activation" -> "jakarta.activation"
            "asm" -> "org.objectweb.asm"
            else -> ref.name
        }

        JpmArchives.moduleToArchive(ModuleLayer.boot().findModule(name).orElseGet { null }
            ?: return@DependencyResolutionBid null)
    }

    public val dependencyResolver: DependencyResolver = moduleResolver.orFallBackOn(ArchiveDependencyResolver())

    public fun exit(e: Exception, extra: String = "A critical error has occurred"): Nothing {
        logger.log(Level.SEVERE, extra)
        logger.log(Level.SEVERE, "YakClient had to quit unexpectedly because : ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }
}

private const val SETTINGS_NAME = "settings.conf"

public fun main(args: Array<String>) {
    val parser = ArgParser("yakclient")

    val yakDirectory by parser.option(PathArgument, "yakdirectory", "d").required()

    parser.parse(args)

    init(yakDirectory)

    val run = runCatching {
        val volume = VolumeStore["api-data"]

        ContainerLoader.load(
            ExtensionInfo(
                Archives.find(YakClient.settings.apiLocation, Archives.Finders.JPM_FINDER),
                YakClient,
            ),
            ExtensionLoader,
            volume,
            PrivilegeManager.allPrivileges(),
            YakClient.loader
        ).process.start()
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

@SuppressWarnings("removal")
public fun init(yakDir: Path, scope: InitScope = InitScope.DEVELOPMENT) {
    Policy.setPolicy(BasicPolicy())
    System.setSecurityManager(SecurityManager())

    if (YakClient.innited) return
    YakClient.innited = true

    registerCustomType(UriCustomType())

    YakClient.yakDir = yakDir
    YakClient.settings = ConfigFactory.parseFile((yakDir resolve SETTINGS_NAME).toFile()).extract("boot")
    YakClient.settings.tempPath.deleteAll()
    YakClient.init(
        JpmArchives.moduleToArchive(YakClient::class.java.module),
        YakClient.settings,
        null
    )

    RepositoryFactory.add(InternalRepoProvider())
    MavenLayoutFactory.add(InternalLayoutProvider())

//    val dl = DependencyGraph.DependencyLoader(
//        RepositoryFactory.create(
//            RepositorySettings(MAVEN_CENTRAL)
//        ),
//        dependencyResolver
//    )

//    if (scope.equalsAny(InitScope.PRODUCTION, InitScope.DEVELOPMENT)) {
//        dl load "org.jetbrains.kotlinx:kotlinx-cli-jvm:0.3.4"
//        dl load "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1"
//        dl load "org.jetbrains.kotlin:kotlin-reflect:1.6.0"
//        dl load "io.github.config4k:config4k:0.4.2"
//        dl load "com.fasterxml.jackson.module:jackson-module-kotlin:2.12.6"
//        dl load "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.6"
//        dl load "org.jetbrains.kotlin:kotlin-stdlib-common:2.0.0-beta-1"
//        dl load "io.ktor:ktor-client-cio:2.0.0"
//        dl load "net.yakclient:archives:1.0-SNAPSHOT"
//        dl load "org.jetbrains.kotlin:kotlin-stdlib:1.6.21"
//        dl load "org.jetbrains.kotlin:kotlin-reflect:1.6.21"
//        dl load "net.yakclient:common-util:1.0-SNAPSHOT"
//    }
}

