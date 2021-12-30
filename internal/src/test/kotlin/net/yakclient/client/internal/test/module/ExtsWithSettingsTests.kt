package net.yakclient.client.internal.test.module

import com.typesafe.config.Config
import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import net.yakclient.client.internal.extension.*
import net.yakclient.client.internal.lifecycle.*
import net.yakclient.client.internal.setting.ExtSettingsReference
import net.yakclient.client.internal.setting.ExtensionSettings
import net.yakclient.client.internal.setting.SettingsAnalyzer
import net.yakclient.client.internal.setting.SettingsInterpreter
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
        val loader = ExtensionLoader(listOf(
            ClassNameRefiner(),
            SettingsAnalyzer(interpreter = {
                ConfigFactory.parseReader(InputStreamReader(it.asInputStream()))
                    .extract<BasicExtensionSettings>("extension")
            }),
            object : ExtLoadingProcess<ExtSettingsReference<out ExtensionSettings>, ExtensionReference> {
                override val accepts: Class<ExtSettingsReference<out ExtensionSettings>> =
                    ExtSettingsReference::class.java

                override fun process(toProcess: ExtSettingsReference<out ExtensionSettings>): ExtensionReference =
                    toProcess.also { println(toProcess.settings) }
            },
            object : ExtensionLinker {
                override fun process(toProcess: ExtensionReference): LinkedExtension =
                    LinkedExtension(ModuleReferenceClassLoader(toProcess))
            },
            object : ExtensionResolver {
                override fun process(toProcess: LinkedExtension): Extension =
                    object : Extension {
                        override val parent: Extension? = null
                        override val loader: ClassLoader = toProcess.classloader
                    }
            }
        ))
        val module = loader.loadJar(workingDir().parent("client").child("api", "build", "libs", "api-1.0-SNAPSHOT.jar"))
            ?: throw IllegalStateException("Failed to load module")

        println(module)
    }
}