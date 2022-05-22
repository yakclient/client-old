package net.yakclient.client.boot.extension

import net.yakclient.common.util.openStream
import net.yakclient.common.util.readInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.util.function.Supplier

//public class ExtensionEntry(
//    override val name: String,
//    private val _uri: Supplier<URI?> = Supplier { null },
//    private val _bytes: Supplier<ByteArray?> = Supplier { null },
//    private val _ins: Supplier<InputStream?> = Supplier { null }
//) : ArchiveHandle.Entry() {
//    private operator fun <T> Supplier<T>.invoke(): T = get()
//
//    override val asUri: URI
//        get() = _uri() ?: throw IllegalArgumentException("Uri not provided")
//    override val asBytes: ByteArray
//        get() = _bytes() ?: _ins()?.readInputStream() ?: _uri()?.openStream()?.readInputStream()
//        ?: throw IllegalArgumentException("Nothing provided, not able to read bytes")
//    override val asInputStream: InputStream
//        get() = _ins() ?: _bytes()?.let { ByteArrayInputStream(it) } ?: _uri()?.openStream()
//        ?: throw IllegalArgumentException("Nothing provided, not able to read stream.")
//}

//@JvmOverloads
//public fun entryOf(
//    name: String,
////    uri: URI? = null,
////    bytes: ByteArray? = null,
////    inputStream: InputStream? = null
//): ArchiveHandle.Entry = ExtensionEntry(name, { uri }, { bytes }, { inputStream })
//
//public fun entryOfClass(className: String): ArchiveHandle.Entry = ExtensionEntry(className, _uri = {
//    ClassLoader.getSystemResource("${className.replace('.', '/')}.class").toURI()
//}, _bytes = {
//    ClassLoader.getSystemResourceAsStream("${className.replace('.', '/')}.class")!!.readInputStream()
//}, _ins = {
//    ClassLoader.getSystemResourceAsStream("${className.replace('.', '/')}.class")
//})