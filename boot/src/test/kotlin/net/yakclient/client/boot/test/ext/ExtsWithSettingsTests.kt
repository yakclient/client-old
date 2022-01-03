package net.yakclient.client.boot.test.ext

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import net.yakclient.client.boot.ext.*
import net.yakclient.client.boot.lifecycle.*
import net.yakclient.client.boot.setting.SettingsAnalyzer
import net.yakclient.client.util.child
import net.yakclient.client.util.parent
import net.yakclient.client.util.workingDir
import org.junit.jupiter.api.Test
import java.io.InputStreamReader

class ExtsWithSettingsTests {
    @Test
    fun `Test Hocon reading`() {
        val config: Config = ConfigFactory.parseFile(
            workingDir().parent("client").child("api", "src", "main", "resources", "ext-settings.conf")
        )
        val settings = config.extract<BasicExtensionSettings>("extension")
        println(settings)
    }

    @Test
    fun `Test API with loading settings file`() {
        val loader = MutableExtLoader()
            .add(ClassNameRefiner())
            .add(SettingsAnalyzer(interpreter = {
                ConfigFactory.parseReader(InputStreamReader(it.asInputStream()))
                    .extract<BasicExtensionSettings>("extension")
            })).add {
                it.also { println(it.settings) }
            }.add {
                ReferenceClassLoader(it)
            }.add {
                object : Extension() {}
            }.toLoader()
//        val loader = ExtensionLoader(listOf(
//            ClassNameRefiner(),
//            SettingsAnalyzer(interpreter = {
//                ConfigFactory.parseReader(InputStreamReader(it.asInputStream()))
//                    .extract<BasicExtensionSettings>("extension")
//            }),
//            object : ExtLoadingProcess<ExtSettingsReference<out ExtensionSettings>, ExtensionReference> {
//                override val accepts: Class<ExtSettingsReference<out ExtensionSettings>> =
//                    ExtSettingsReference::class.java
//
//                override fun process(toProcess: ExtSettingsReference<out ExtensionSettings>): ExtensionReference =
//                    toProcess.also { println(toProcess.settings) }
//            },
//            object : ExtensionLinker {
//                override fun process(toProcess: ExtensionReference): LinkedExtension =
//                    LinkedExtension(ModuleReferenceClassLoader(toProcess))
//            },
//            object : ExtensionResolver {
//                override fun process(toProcess: LinkedExtension): Extension =
//                    object : Extension {
//                        override val parent: Extension? = null
//                        override val loader: ClassLoader = toProcess.classloader
//                    }
//            }
//        ))
        val module =
            loader.loadJar(workingDir().parent("client").child("api", "build", "libs", "api-1.0-SNAPSHOT.jar").toURI())
                ?: throw IllegalStateException("Failed to load module")

        println(module)
    }
}