package net.yakclient.client.boot.test.ext


class TestExtLoading {
//    @Test
//    fun `Test basic extension pipeline`() {
//        val loader = MutableExtLoader().add(ClassNameRefiner()).add {
//            AnalyzedExtReference(it.toMutableMap().also { map ->
//                val config = TransformerConfig.of {
//                    transformClass { node ->
//                        node.apply { node.name = "net/yakclient/generated/SomeGeneratedClass" }
//                    }
//                    transformMethod { node ->
//                        node.instructions = InstructionAdapters.AlterThisReference(
//                            ProvidedInstructionReader(node.instructions),
//                            "net/yakclient/generated/SomeGeneratedClass",
//                            "net/yakclient/client/internal/test/module/ClassToBeInjected"
//                        ).get()
//                        node
//                    }
//                    transformMethod(TargetedMethodTransformer(ByteCodeUtils.MethodSignature.of("<init>()V")) { node ->
//                        node.instructions.filterIsInstance<LdcInsnNode>().first().cst = "haha"
//                        node
//                    })
//                }
//
//                val resolve = Bmu.resolve(ClassReader(ClassToBeInjected::class.java.name), config)
//
//                map["net.yakclient.generated.SomeGeneratedClass"] =
//                    NamedExtensionEntry("net.yakclient.generated.SomeGeneratedClass") {
//                        ByteArrayInputStream(resolve)
//                    }
//            })
//        }.add {
//            ModuleReferenceClassLoader(it)
//        }.add {
//            object : Extension() {  }
//        }.toLoader()
//
//        val module = loader.load(
//            JarReferencer().process(
//                workingDir().parent("client").child("api", "build", "libs").first()
//                    .toURI()
//            )
//        )
//        val clazz = module.loader.loadClass("net.yakclient.generated.SomeGeneratedClass")
//        val obj = clazz.getConstructor().newInstance()
//        clazz.getMethod("print").invoke(obj)
//    }
}

class ClassToBeInjected {
    @JvmField
    val value: String = "I better print this...."

    fun print() {
        println(value)
    }
}