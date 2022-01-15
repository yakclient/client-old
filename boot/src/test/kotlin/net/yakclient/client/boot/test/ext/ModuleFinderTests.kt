package net.yakclient.client.boot.test.ext

import net.yakclient.bmu.api.Bmu
import net.yakclient.bmu.api.TransformerConfig
import net.yakclient.client.util.child
import net.yakclient.client.util.parent
import net.yakclient.client.util.workingDir
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ModuleVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.lang.module.ModuleFinder
import java.lang.module.ModuleReference
import java.util.*
import java.util.jar.JarFile

class ModuleFinderTests {
    @Test
    fun `Create Module Finder`() {
        val finder = object : ModuleFinder {
            override fun find(name: String?): Optional<ModuleReference> {
                TODO("Not yet implemented")
            }

            override fun findAll(): MutableSet<ModuleReference> {
                TODO("Not yet implemented")
            }

        }
    }

    @Test
    fun `Test read module-info from jar`() {
        val jar = JarFile(workingDir().parent("client").child("api", "build", "libs", "api-1.0-SNAPSHOT.jar"))
        val main = jar.getJarEntry("net.yakclient.client.api.ApiExtension\"")

        Bmu.resolve(ClassReader(jar.getInputStream(main)), TransformerConfig.of {
            transformClass {
                println(it.module.name)
                it
            }
        })
    }
}