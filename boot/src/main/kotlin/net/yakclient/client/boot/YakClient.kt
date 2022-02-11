package net.yakclient.client.boot

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.github.config4k.registerCustomType
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.newFixedThreadPoolContext
import net.yakclient.client.boot.archive.ArchiveUtils
import net.yakclient.client.boot.dep.CachedDependency
import net.yakclient.client.boot.dep.DependencyGraph
import net.yakclient.client.boot.dep.DependencyNode
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader
import net.yakclient.client.boot.internal.jpm.ResolvedJpm
import net.yakclient.client.util.*
import java.lang.module.ModuleDescriptor
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

private const val DEFAULT_THREAD_POOL_SIZE = 10

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
    val threadPoolSize by parser.option(ArgType.Int, "poolsize")

    parser.parse(args).run {
        init(yakDirectory, threadPoolSize ?: DEFAULT_THREAD_POOL_SIZE)
    }

    ExtensionLoader.load(ArchiveUtils.find(YakClient.settings.apiLocation), YakClient).onLoad()
//    ExtensionLoader.load(ExtensionLoader.find(YakClient.settings.apiLocation), YakClient).onLoad()
}

public fun init(yakDir: Path, poolSize: Int) {
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

    val loaded = HashMap<String, DependencyNode>()

    fun loadDependencies(module: Module): DependencyNode {
        val desc = module.descriptor
        val dependencies: Set<DependencyNode> = desc.requires()
            .asSequence()
            .map(ModuleDescriptor.Requires::name)
            .map(ModuleLayer.boot()::findModule)
            .filter(Optional<Module>::isPresent)
            .map(Optional<Module>::get)
            .map { m ->
                if (loaded.contains(m.name)) loaded[m.name]!!
                else loadDependencies(m)
            }.toSet()

        val dep = DependencyNode(
            CachedDependency.Descriptor(desc.name().replace('.', '-'), desc.version().orElse(null)?.toString()),
            ResolvedJpm(module),
            dependencies
        )
        loaded[dep.desc.artifact] = dep
        return dep
    }

    val map = YakClient::class.java.module.layer.modules().map(::loadDependencies)
    map.forEach(DependencyGraph::forceAdd)
}

