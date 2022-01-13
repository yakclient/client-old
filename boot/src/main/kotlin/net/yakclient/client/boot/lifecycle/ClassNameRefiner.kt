package net.yakclient.client.boot.lifecycle

//// TODO look into making this an object, its a bit weird as it is
//public val ClassNameRefiner: ExtensionTransformer = ExtensionTransformer { ref ->
//    ExtensionReference(ref.mapKeys {
//        if (it.key.endsWith(".class")) it.key.replace('/', '.').removeSuffix(".class") else it.key
//    })
//}