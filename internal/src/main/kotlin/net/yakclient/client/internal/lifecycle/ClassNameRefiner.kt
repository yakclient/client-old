package net.yakclient.client.internal.lifecycle

import net.yakclient.client.internal.extension.ExtensionReference
import net.yakclient.client.internal.extension.ExtensionTransformer

public class ClassNameRefiner : ExtensionTransformer {
    override fun process(toProcess: ExtensionReference): ExtensionReference = ExtensionReference(toProcess.mapKeys {
        if (it.key.endsWith(".class")) it.key.replace('/', '.').removeSuffix(".class") else it.key
    })
}