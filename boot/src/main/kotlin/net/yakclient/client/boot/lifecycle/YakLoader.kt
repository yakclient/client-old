package net.yakclient.client.boot.lifecycle

import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths


internal class YakLoader(parent: ClassLoader?) : URLClassLoader(arrayOf<URL>(), parent) {
//    override fun defineClass(name: String, bytes: ByteArray): Class<*> = super.defineClass(name, bytes, 0, bytes.size)

    fun appendToClassPathForInstrumentation(path: String) {
        assert(Thread.holdsLock(this))
        super.addURL(Paths.get(path).toUri().toURL())
    }
}