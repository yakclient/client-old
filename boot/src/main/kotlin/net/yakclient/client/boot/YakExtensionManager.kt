package net.yakclient.client.boot

import net.yakclient.client.boot.ext.*
import net.yakclient.client.boot.lifecycle.*
import net.yakclient.client.util.readInputStream

//public object YakExtensionManager {
////    public val api: Extension = run {
////        try {
////            println("should only happen once")
////            extLoader(YakClient.boot).loadJar(YakClient.settings.apiLocation)
////        } catch (e: Exception) {
////            YakClient.exit(e, "Failed to load the Yak API!")
////        }
////    }
//
////    public var apiInternal: Extension by immutableLateInit()
//
//    //    by lazy {
////        try {
////            val moduleLoader = moduleLoader(null)
////            moduleLoader.loadJar(YakClient.settings.apiInternalLocation)
////        } catch (e: Exception) {
////            YakClient.exit(e, "Failed to load the Yak Internal API!")
////        }
////    }
////    public lateinit var minecraft: Extension// by immutableLateInit()
//
//
//    public fun extLoader(parent: Extension): ExtensionLoader = MutableExtLoader()
//        .add(ClassNameRefiner)
//        .add(hoconAnalyzer<BasicExtensionSettings>()).add { ref ->
//            val type = ref.settings.loader?.let { l ->
//                val ips = ref.load(l)?.asInputStream()
//                    ?: throw IllegalArgumentException("Non-null setting \"loader\" with value $l cannot be found in the referenced extension.")
//                val bytes = readInputStream(ips)
//                val loader = (parent.loader as? ClassDefiner ?: throw IllegalArgumentException("Parent loader must be able to define classes. Please implement ${ClassDefiner::class.qualifiedName}")).defineClass(l, bytes)
//                if (!ReferenceClassLoader::class.java.isAssignableFrom(loader)) throw IllegalStateException("Invalid extension class loader : ${loader.name}, Should inherit from ReferenceClassLoader and DelegatedLoader")
//                loader
//            }
//            val loader = type?.getConstructor(ClassLoader::class.java,ExtensionReference::class.java)?.newInstance(parent.loader, ref)
//9
//            (loader as? ClassLoader ?: ReferenceClassLoader(parent.loader, ref) ) to ref
//        }.add { (loader, ref) ->
//            val settings = ref.settings
//
//            check(ref.exists(settings.extensionClass)) { "Failed to find class ${settings.extensionClass} in when loading extension." }
//            val extensionClass = loader.loadClass(settings.extensionClass)
//
//            check(Extension::class.java.isAssignableFrom(extensionClass)) { "Extension main class does not inherit from ${Extension::class.java.name}!" }
//
//            ExtInitProperties(extensionClass.getConstructor().newInstance() as Extension, loader, parent, settings)
//
//        }.add(ExtensionInitializer()).add {
//            it.onLoad()
//            it
//        }.toLoader()
//
////    public fun initExtension(
////        parent: Extension?
////    ): ExtLoadingProcess<Pair<ClassLoader, ExtSettingsReference<ExtensionSettings>>, ExtInitProperties> =
////        ExtLoadingProcess { (loader, ref) ->
////            val settings = ref.settings
////
////            check(ref.exists(settings.extensionClass)) { "Failed to find class ${settings.extensionClass} in when loading extension." }
////            val extensionClass = loader.loadClass(settings.extensionClass)
////
////            check(Extension::class.java.isAssignableFrom(extensionClass)) { "Extension main class does not inherit from ${Extension::class.java.name}!" }
////
////            ExtInitProperties(extensionClass.getConstructor().newInstance() as Extension, loader, parent)
////        }
//}