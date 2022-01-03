package net.yakclient.client.boot.ext

import net.yakclient.client.boot.setting.ExtensionSettings
import net.yakclient.client.util.immutableLateInit
import java.util.logging.Logger
import kotlin.properties.Delegates

public abstract class Extension {
    private var initialized: Boolean = false

    public var parent: Extension? by Delegates.vetoable(null) { _, _, _ -> !initialized }
    public var loader: ClassLoader by immutableLateInit()
    private var settings: ExtensionSettings by immutableLateInit()
    public val logger: Logger = Logger.getLogger("@unnamedExtension")

    public fun init(loader: ClassLoader, settings: ExtensionSettings, parent: Extension? = null) {
        if (initialized) return

        this.loader = loader
        this.settings = settings
        this.parent = parent

        initialized = true
    }

    public open fun onLoad() {}
}