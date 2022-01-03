package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.ext.ExtLoadingProcess
import net.yakclient.client.boot.ext.ExtensionReference
import java.net.URI

public fun augmentReference(vararg refs: URI): ExtLoadingProcess<ExtensionReference, ExtensionReference> =
    ExtLoadingProcess {
        ExtensionReference(it.toMutableMap().also { refs.forEach { uri -> loadJar(uri) } })
    }
