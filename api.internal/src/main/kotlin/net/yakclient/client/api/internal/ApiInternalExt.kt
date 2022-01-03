package net.yakclient.client.api.internal

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.YakExtensionManager
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.plus
import net.yakclient.client.boot.lifecycle.loadJar

public class ApiInternalExt : Extension() {
    override fun onLoad() {
        YakExtensionManager.extLoader(this).load(loadJar(YakClient.settings.mcExtLocation) + loadJar(YakClient.settings.mcLocation))

//        val toLoader = MutableExtLoader()
//            .add(augmentReference(YakClient.settings.mcLocation)).add(ClassNameRefiner)
//            .add(hoconAnalyzer<BasicExtensionSettings>())
//            .add {
//                (ModuleReferenceClassLoader(it) as ClassLoader) to (it as ExtSettingsReference<ExtensionSettings>)
//            }.add { (loader, ref) ->
//                val settings = ref.settings
//
//                check(ref.exists(settings.extensionClass)) { "Failed to find class ${settings.extensionClass} in when loading extension." }
//                val extensionClass = loader.loadClass(settings.extensionClass)
//
//                check(Extension::class.java.isAssignableFrom(extensionClass)) { "Extension main class does not inherit from ${Extension::class.java.name}!" }
//
//                ExtInitProperties(extensionClass.getConstructor().newInstance() as Extension, loader, parent)
//            }.add(ExtensionInitializer()).add {
//                it.onLoad()
//                it
//            }.toLoader()
//            toLoader.loadJar(YakClient.settings.mcExtLocation)
    }
}