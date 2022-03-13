package net.yakclient.client.api.restricted

import com.typesafe.config.ConfigFactory
import net.yakclient.client.boot.exception.RestrictedClassException
import net.yakclient.client.boot.archive.ArchiveReference
import net.yakclient.client.boot.lifecycle.ExtClassLoader

public class ApiExtLoader(parent: ClassLoader, reference: ArchiveReference) : ExtClassLoader(parent, reference) {
    private val specifics: Set<String> = ConfigFactory.parseURL(super.findResource("ext-settings.conf")).getStringList("runtime.specifics").toHashSet()

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        if (specifics.contains(name)) throw RestrictedClassException(name)
        return super.loadClass(name, resolve)
    }
}