package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.ext.ExtensionReference
import net.yakclient.client.boot.ext.ExtensionTransformer

// This
public val ClassNameRefiner: ExtensionTransformer = ExtensionTransformer { ref ->
    ExtensionReference(ref.mapKeys {
        if (it.key.endsWith(".class")) it.key.replace('/', '.').removeSuffix(".class") else it.key
    })
}


// Instead of this
//public class ClassNameRefiner : ExtensionTransformer {
//    override fun process(toProcess: ExtensionReference): ExtensionReference = ExtensionReference(toProcess.mapKeys {
//        if (it.key.endsWith(".class")) it.key.replace('/', '.').removeSuffix(".class") else it.key
//    })
//}