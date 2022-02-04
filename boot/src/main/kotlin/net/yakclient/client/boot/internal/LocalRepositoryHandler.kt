package net.yakclient.client.boot.internal

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.dep.CachedDependency
import net.yakclient.client.boot.dep.Dependency
import net.yakclient.client.boot.dep.DependencyGraph
import net.yakclient.client.boot.repository.RepositoryHandler
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType
import java.nio.file.Files
import java.nio.file.Path

private const val META_NAME = "dependencies-meta.conf"

internal open class LocalRepositoryHandler(
    override val settings: RepositorySettings
) : RepositoryHandler<LocalDescriptor> {
    private val path: Path = Path.of(run {
        val it: String = settings.path!!
        if (it.startsWith("~/")) "${YakClient.yakDir.path}${it.removePrefix("~")}" else it
    })
    private val meta = ConfigFactory.parseFile(path.resolve("dependencies-meta.conf").toFile()).extract<Map<String, CachedDependency>>()
    private val dependencies: Map<Dependency.Descriptor, Dependency> =  object : HashMap<Dependency.Descriptor, Dependency>() {
        override fun get(key: Dependency.Descriptor): Dependency? {
            if (!containsKey(key)) {
                meta["${key.artifact}:${key.version}"]?.also {  put(key, Dependency(it.path.toUri(), it.dependants, it.desc)) }
            }
            return super.get(key)
        }
    }

    override fun find(desc: LocalDescriptor): Dependency?  = dependencies[desc]

    override fun loadDescription(dep: String): LocalDescriptor? {
        if (!Files.exists(path.resolve(dep))) return null

        val name = dep.removeSuffix(".jar")
        val start = Regex("-(\\d+(\\.|\$))").find(name)?.range?.first ?: return null

        return LocalDescriptor(name.substring(0, start), name.substring(start + 1))
    }
}