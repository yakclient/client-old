package net.yakclient.client.boot.test.ext


class TestExtLoading {
//    @Testi
//    fun `Test basic extension pipeline`() {
//        val loader = MutableExtLoader().add(ClassNameRefiner).add {
//            ExtensionReference(it.toMutableMap().also { map ->
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
//                    object : ExtensionEntry {
//                        override val name = "net.yakclient.generated.SomeGeneratedClass"
//
//                        override fun asURI(): Nothing = throw UnsupportedOperationException("No URI available")
//
//                        override fun asInputStream(): InputStream = ByteArrayInputStream(resolve)
//                    }
//            })
//        }.add {
//            ExtClassLoader(ClassLoader.getSystemClassLoader(), it)
//        }.add {
//            object : Extension() {}
//        }.toLoader()
//
//        val module = loader.load(
//            loadJar(
//                workingDir().parent("client").child("api", "build", "libs").first()
//                    .toURI()
//            )
//        )
//        val clazz = module.loader.loadClass("net.yakclient.generated.SomeGeneratedClass")
//        val obj = clazz.getConstructor().newInstance()
//        clazz.getMethod("print").invoke(obj)
//
//    }
}

class ClassToBeInjected {
    @JvmField
    val value: String = "I better print this...."

    fun print() {
        println(value)
    }
}