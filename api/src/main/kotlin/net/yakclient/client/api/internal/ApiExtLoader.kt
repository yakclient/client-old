package net.yakclient.client.api.internal

import com.typesafe.config.ConfigFactory
import net.yakclient.client.boot.exception.RestrictedClassException
import net.yakclient.client.boot.ext.ExtReference
import net.yakclient.client.boot.lifecycle.ExtClassLoader

public class ApiExtLoader(parent: ClassLoader, reference: ExtReference) : ExtClassLoader(parent, reference) {
    private val specifics: Set<String> = ConfigFactory.parseURL(super.findResource("ext-settings.conf")).getStringList("runtime.specifics").toHashSet()

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        if (specifics.contains(name)) throw RestrictedClassException(name)
        return super.loadClass(name, resolve)
    }
}