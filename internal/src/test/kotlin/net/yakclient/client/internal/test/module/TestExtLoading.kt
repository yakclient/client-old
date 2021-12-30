package net.yakclient.client.internal.test.module

import net.yakclient.bmu.api.*
import net.yakclient.bmu.api.mixin.InstructionAdapters
import net.yakclient.client.internal.lifecycle.JarModuleReferencer
import net.yakclient.client.internal.lifecycle.ModuleReferenceClassLoader
import net.yakclient.client.internal.extension.*
import net.yakclient.client.internal.lifecycle.ClassNameRefiner
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.LdcInsnNode
import java.io.ByteArrayInputStream
import java.io.File


class TestExtLoading {
    @Test
    fun `Test basic extension pipeline`() {
        val loader = ExtensionLoader(buildList {
            add(ClassNameRefiner())
            add(object : ExtensionAnalyzer {
                override fun process(toProcess: ExtensionReference): AnalyzedExtReference =
                    AnalyzedExtReference(toProcess.toMutableMap().also { map ->
                        val config = TransformerConfig.of {
                            transformClass { node ->
                                node.apply { node.name = "net/yakclient/generated/SomeGeneratedClass" }
                            }
                            transformMethod { node ->
                                node.instructions = InstructionAdapters.AlterThisReference(
                                    ProvidedInstructionReader(node.instructions),
                                    "net/yakclient/generated/SomeGeneratedClass",
                                    "net/yakclient/client/internal/test/module/ClassToBeInjected"
                                ).get()
                                node
                            }
                            transformMethod(TargetedMethodTransformer(ByteCodeUtils.MethodSignature.of("<init>()V")) { node ->
                                node.instructions.filterIsInstance<LdcInsnNode>().first().cst = "haha"
                                node
                            })
                        }

                        val resolve = Bmu.resolve(ClassReader(ClassToBeInjected::class.java.name), config)

                        map["net.yakclient.generated.SomeGeneratedClass"] = NamedExtensionEntry("net.yakclient.generated.SomeGeneratedClass") {
                            ByteArrayInputStream(resolve)
                        }
                    })
            })
            add(object : ExtensionLinker {
                override fun process(toProcess: ExtensionReference): LinkedExtension {
                    return LinkedExtension(ModuleReferenceClassLoader(toProcess))
                }
            })
            add(object : ExtensionResolver {
                override fun process(toProcess: LinkedExtension): Extension =
                    object : Extension {
                        override val parent: Extension? = null
                        override val loader: ClassLoader = toProcess.classloader
                    }
            })
        })



        val module = loader.load(
            JarModuleReferencer().process(
                workingDir().parent("client").child("api", "build", "libs").listFiles().first()
                    .toURI()
            )
        ) ?: throw IllegalStateException("Failed to load module")
        val clazz = module.loader.loadClass("net.yakclient.generated.SomeGeneratedClass")
        val obj = clazz.getConstructor().newInstance()
        clazz.getMethod("print").invoke(obj)
    }
}

class ClassToBeInjected {
    @JvmField
    val value: String = "I better print this...."

    fun print() {
        println(value)
    }
}