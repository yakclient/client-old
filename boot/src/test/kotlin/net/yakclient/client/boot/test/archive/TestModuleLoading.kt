package net.yakclient.client.boot.test.archive

import net.yakclient.client.boot.init
import net.yakclient.client.util.*
import java.io.File
import java.lang.module.Configuration
import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.FileSystems
import java.security.CodeSource
import java.security.ProtectionDomain
import java.security.cert.Certificate
import java.util.jar.JarFile
import java.util.zip.ZipFile
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestModuleLoading {
    @BeforeTest
    fun setup() {
        init(workingDir().parent("client").child("workingDir").toPath())
    }

    @Test
    fun `Load module with custom classloader`() {
        val finder =
            ModuleFinder.of(workingDir().parent("client").child("workingDir", "cache", "lib", "asm-9.2.jar").toPath())

        val config = Configuration.resolve(
            finder,
            listOf(ModuleLayer.boot().configuration()),
            ModuleFinder.of(),
            finder.findAll().map(ModuleReference::descriptor).map(ModuleDescriptor::name)
        )

        val loader = object : ClassLoader() {
            private val reference = finder.find("org.objectweb.asm").get()
            private val domain = ProtectionDomain(CodeSource(null, arrayOf<Certificate>()), null, this, null)

            override fun findClass(name: String): Class<*> {
                findLoadedClass(name)?.let { return@findClass it }

                val bb = reference.open().open("${name.replace('.', '/')}.class").orElseThrow()
                return defineClass(name, ByteBuffer.wrap(bb.readInputStream()), domain)
            }
        }

        val controller = ModuleLayer.defineModules(
            config,
            listOf(ModuleLayer.boot()),
        ) { loader }

        val layer = controller.layer()

        val module = layer.modules().first()

        assert(module.classLoader.loadClass("org.objectweb.asm.Frame").module?.name == "org.objectweb.asm")
    }

    @Test
    fun `Test read entry in zip file`() {
        val location = checkNotNull(this::class.java.getResource("/zip-test.zip"))
        val zip = ZipFile(location.file)

        zip.use {
            val entry = zip.getEntry("testFile")
            println(zip.getInputStream(entry).readAllBytes().toString(Charsets.UTF_8))

            val uri = URI("jar:${location}!/${entry.name}")
            println(uri.openStream().readAllBytes().toString(Charsets.UTF_8))
        }
    }
}