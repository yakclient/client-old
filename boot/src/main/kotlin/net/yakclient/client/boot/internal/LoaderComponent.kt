//package net.yakclient.client.boot.internal
//
//internal class LoaderComponent(
//    private val loader: ClassLoader
//) : ClComponent {
//    override fun find(name: String): Class<*>? = loader.runCatching { loader.loadClass(name) }.getOrNull()
//}