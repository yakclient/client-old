package net.yakclient.client.boot.lifecycle

import net.yakclient.client.boot.ext.ExtLoadingProcess
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.setting.ExtensionSettings

public class ExtensionInitializer : ExtLoadingProcess<ExtInitProperties, Extension> {
    override fun process(toProcess: ExtInitProperties): Extension = toProcess.let { (ext, loader, parent, settings) -> ext.init(loader, settings, parent); ext }
}

public data class ExtInitProperties(
    internal val ext: Extension,
    internal val loader: ClassLoader,
    internal val parent: Extension?,
    internal val settings: ExtensionSettings,
)