package net.yakclient.client.boot.extension

import net.yakclient.archives.ResolvedArchive
import net.yakclient.client.boot.container.ContainerProcess
import net.yakclient.client.boot.exception.AlreadyInitializedException
import net.yakclient.common.util.immutableLateInit
import java.util.logging.Logger
import kotlin.properties.Delegates

public abstract class Extension : ContainerProcess {
    private var initialized: Boolean = false

    public var parent: Extension? by Delegates.vetoable(null) { _, _, _ -> !initialized }
    public val loader: ClassLoader
        get() = ref.classloader
    internal var ref: ResolvedArchive by immutableLateInit()
    private var settings: ExtensionSettings by immutableLateInit()
    public var logger: Logger by immutableLateInit()

    public fun init(ref: ResolvedArchive, settings: ExtensionSettings, parent: Extension? = null) {
        if (initialized) throw AlreadyInitializedException(this::class)

        this.ref = ref
        this.settings = settings
        this.parent = parent
        this.logger = Logger.getLogger(settings.name)

        initialized = true
    }

    final override fun start(): Unit = onLoad()

    public open fun onLoad() {}

    override fun toString(): String = "Extension(name=${settings.name}, parent=$parent, loader=$loader)"
}