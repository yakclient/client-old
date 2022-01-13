package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.ext.ExtReference

public open class ExtClassLoader(
    parent: ClassLoader,
    protected val reference: ExtReference
) : ClassLoader(parent), ClassDefiner {
    override fun defineClass(name: String, bytes: ByteArray) : Class<*> = super.defineClass(name, bytes, 0, bytes.size)
}
