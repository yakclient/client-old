package net.yakclient.client.boot

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.github.config4k.registerCustomType
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.coroutines.newFixedThreadPoolContext
import net.yakclient.client.boot.dep.CachedDependency
import net.yakclient.client.boot.dep.DependencyGraph
import net.yakclient.client.boot.dep.DependencyNode
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader
import net.yakclient.client.boot.internal.JpmDependencyGraph
import net.yakclient.client.boot.internal.JpmDependencyReference
import net.yakclient.client.util.FileArgument
import net.yakclient.client.util.UriCustomType
import net.yakclient.client.util.child
import net.yakclient.client.util.immutableLateInit
import java.io.File
import java.lang.module.ModuleDescriptor
import java.util.*
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

private const val DEFAULT_THREAD_POOL_SIZE = 10

public object YakClient : Extension() {
    internal var innited: Boolean = false
    public var settings: BootSettings by immutableLateInit()
    public var yakDir: File by immutableLateInit()
    public var theGraph: DependencyGraph by immutableLateInit()

    public var coroutineContext: CoroutineContext by immutableLateInit()

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

    val yakDirectory by parser.option(FileArgument, "yakdirectory", "d").required()
    val threadPoolSize by parser.option(ArgType.Int, "poolsize")

    parser.parse(args).run {
        init(yakDirectory, threadPoolSize ?: DEFAULT_THREAD_POOL_SIZE)
    }

    ExtensionLoader.load(ExtensionLoader.find(YakClient.settings.apiLocation), YakClient).onLoad()
}

public fun init(yakDir: File, poolSize: Int) {
    if (YakClient.innited) return
    YakClient.innited = true

    registerCustomType(UriCustomType())

    val settings: BootSettings = ConfigFactory.parseFile(yakDir.child(SETTINGS_NAME)).extract("boot")

    YakClient.yakDir = yakDir
    YakClient.settings = settings
    YakClient.theGraph = JpmDependencyGraph()
    YakClient.coroutineContext =
        newFixedThreadPoolContext( // Should not use delicate API's but until something new is implemented this is it.
            poolSize,
            "YakClient Context"
        )

    YakClient.init(
        ClassLoader.getSystemClassLoader(),
        settings,
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
            CachedDependency.Descriptor(desc.name(), desc.version().orElse(null)?.toString()),
            JpmDependencyReference(module),
            dependencies
        )
        loaded[dep.desc.artifact] = dep
        return dep
    }

    val map = YakClient::class.java.module.layer.modules().map(::loadDependencies)
    map.forEach(YakClient.theGraph::forceAdd)
}

