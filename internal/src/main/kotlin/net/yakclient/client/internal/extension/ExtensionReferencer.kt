package net.yakclient.client.internal.extension

import java.net.URI

public interface ExtensionReferencer : ExtLoadingProcess<URI, ExtensionReference> {
    override val accepts: Class<URI>
        get() = URI::class.java
}