package net.yakclient.client.boot

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.github.config4k.registerCustomType
import kotlinx.cli.ArgParser
import kotlinx.cli.required
import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.archive.ResolvedArchive
import net.yakclient.client.boot.dep.DependencyGraph
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader
import net.yakclient.client.boot.internal.jpm.ResolvedJpmArchive
import net.yakclient.client.boot.internal.maven.MavenDescriptor
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType
import net.yakclient.client.util.*
import java.net.URI
import java.nio.file.Path
import java.util.logging.Level
import kotlin.system.exitProcess


public object YakClient : Extension() {
    internal var innited: Boolean = false
    public var settings: BootSettings by immutableLateInit()
    public var yakDir: Path by immutableLateInit()

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
//    val threadPoolSize by parser.option(ArgType.Int, "poolsize")

    parser.parse(args)

    init(yakDirectory)

    val run = runCatching {
        ExtensionLoader.load(ArchiveUtils.find(YakClient.settings.apiLocation), YakClient).onLoad()
    }

    if (run.isFailure) {
        YakClient.logger.log(Level.INFO, "Error occurred, Exiting gracefully")
        run.exceptionOrNull()?.printStackTrace()
    } else YakClient.logger.log(Level.INFO, "Successfully Quit")
}

public fun init(yakDir: Path) {
    if (YakClient.innited) return
    YakClient.innited = true

    registerCustomType(UriCustomType())

    YakClient.yakDir = yakDir

    YakClient.settings = ConfigFactory.parseFile((yakDir resolve SETTINGS_NAME).toFile()).extract("boot")

    // Clear temp directory
    YakClient.settings.tempPath.deleteAll()

    YakClient.init(
        ResolvedJpmArchive(
            YakClient::class.java.module,
            object : ArchiveReference {
                override val name: String = "yakclient.client.boot"
                override val location: URI
                    get() = throw UnsupportedOperationException()
                override val reader: ArchiveReference.Reader
                    get() = throw UnsupportedOperationException()
                override val writer: ArchiveReference.Writer
                    get() = throw UnsupportedOperationException()
                override val modified: Boolean = false
            }),
        YakClient.settings,
        null
    )

//    val mvn = RepositoryFactory.create(RepositorySettings(RepositoryType.MAVEN_CENTRAL, null)) as RepositoryHandler<Dependency.Descriptor>
//
//    mvn.find(mvn.loadDescription("")!!)

    val populator = object : DependencyGraph.DependencyLoader<MavenDescriptor>(
        RepositoryFactory.create(
            RepositorySettings(RepositoryType.MAVEN_CENTRAL, null)
        ) as RepositoryHandler<MavenDescriptor>
    ) {
        override fun resolve(archive: ArchiveReference, dependants: List<ResolvedArchive>): ResolvedArchive {
            return ResolvedJpmArchive(
                ModuleLayer.boot().modules().find {
                    val n = archive.name
                    it.name == n
//                    it.name == (if (n.startsWith("kotlinx")) "$n.jvm" else n) // Absolutely terrible, but i dont want to find out why kotlinx things randomly have .jvm after them...
                } ?: return super.resolve(archive, dependants),
                archive
            )
        }
    }

    populator.load("org.jetbrains.kotlinx:kotlinx-cli-jvm:0.3.4")
    populator.load("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.0")
    populator.load("org.jetbrains.kotlin:kotlin-reflect:1.6.0")
    populator.load("io.github.config4k:config4k:0.4.2")
    populator.load("com.typesafe:config:1.4.1")
    populator.load("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    populator.load("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.3")

//    val loaded = HashMap<String, DependencyNode>()

//    fun loadDependencies(module: Module): DependencyNode {
//        val desc = module.descriptor
//        val dependencies: Set<DependencyNode> = desc.requires()
//            .asSequence()
//            .map(ModuleDescriptor.Requires::name)
//            .map(ModuleLayer.boot()::findModule)
//            .filter(Optional<Module>::isPresent)
//            .map(Optional<Module>::get)
//            .map { m ->
//                if (loaded.contains(m.name)) loaded[m.name]!!
//                else loadDependencies(m)
//            }.toSet()
//
//        val dep = DependencyNode(
//            CachedDependency.Descriptor(desc.name().replace('.', '-'), desc.version().orElse(null)?.toString()),
//            ResolvedJpm(module),
//            dependencies
//        )
//        loaded[dep.desc.artifact] = dep
//        return dep
//    }
//
    val map = ModuleLayer.boot().modules()
        .filterNot { it.name.startsWith("java") }
        .filterNot { it.name.startsWith("jdk") }
        .filterNot { it.name.startsWith("yakclient") }
//        .map(::loadDependencies)
//    map.forEach(DependencyGraph::forceAdd)
}

