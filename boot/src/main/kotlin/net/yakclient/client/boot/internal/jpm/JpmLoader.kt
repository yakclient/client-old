package net.yakclient.client.boot.internal.jpm

//internal open class JpmLoader(
//    parent: ClassLoader,
//    ref: JpmReference,
//    externals: List<ResolvedJpm>
//) : ArchiveLoader(parent, ref) {
//    //    private val externalClasses: Map<String, ClassLoader>
//    private val loaders: Set<ClassLoader> = externals.mapTo(HashSet()) { it.classloader }
//
//    private val domain = ProtectionDomain(CodeSource(null, arrayOf<Certificate>()), null, this, null)
//
//    init {
////        val e = externals.map { it as JpmReference }
////        val readablePackages = e.flatMapTo(HashSet()) { a ->
////            val descriptor = a.descriptor()
////            if (descriptor.isAutomatic) descriptor.packages() else descriptor.exports().map { it.source() }
////        }
////
////        externalClasses = externals.flatMap { resolved ->
////            resolved.reference.reader.entries().filter { it.name.endsWith(".class") }
////                .filter {
////                    val entryPackage = it.name.substring(it.name.lastIndexOf('.'))
////                    readablePackages.contains(entryPackage)
////                }.map { it.name to resolved.classloader }
////        }.associate { it }
//    }
//
//    // TODO this is pretty slow as finding a single class requires looping through all of the external archives.
//    private fun loadExternalClass(name: String): Class<*>? = loaders.firstNotNullOfOrNull { it.loadClassOrNull(name) }
//
//
//    override fun findClass(name: String): Class<*> = loadExternalClass(name) ?: throw ClassNotFoundException(name)
//
//    override fun loadClass(name: String, resolve: Boolean): Class<*> = findLoadedClass(name) ?: run {
//        val entry: ArchiveReference.Entry =
//            ref.reader["${name.replace('.', '/')}.class"] ?: return super.loadClass(name, resolve)
//
//        val bb = ByteBuffer.wrap(entry.asBytes)
//
//        return defineClass(name, bb, domain).also { if (resolve) resolveClass(it) }
//    }
//
//    override fun findResource(name: String): URL? = ref.reader[name]?.asUri?.toURL()
//}