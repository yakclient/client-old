package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.archive.ArchiveHandle

public open class ExtClassLoader(
    parent: ClassLoader,
    protected val reference: ArchiveHandle
) : ClassLoader(parent) {
//    override fun defineClass(name: String, bytes: ByteArray) : Class<*> = super.defineClass(name, bytes, 0, bytes.size)
}
