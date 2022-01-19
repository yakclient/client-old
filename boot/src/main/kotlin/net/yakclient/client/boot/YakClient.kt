package net.yakclient.client.boot

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.config4k.ClassContainer
import io.github.config4k.extract
import io.github.config4k.registerCustomType
import kotlinx.cli.ArgParser
import kotlinx.cli.required
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.ext.ExtensionLoader
import net.yakclient.client.boot.repository.RepositoryFactory
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.boot.repository.RepositoryType
import net.yakclient.client.util.*
import java.io.File
import java.util.logging.Level
import kotlin.system.exitProcess

public object YakClient : Extension() {
    public var settings: BootSettings by immutableLateInit()
    public var yakDir: File by immutableLateInit()

//    override fun init(loader: ClassLoader, settings: ExtensionSettings, parent: Extension?) {
//        assert(parent == null) { "YakClient Boot cannot have a parent" }
//
//        this.settings = settings as? BootSettings ?: exit(IllegalArgumentException("Illegal settings type"))
//        super.init(loader, settings, parent)
//    }

    public fun exit(e: Exception, extra: String = "A critical error has occurred"): Nothing {
        logger.log(Level.SEVERE, extra)
        logger.log(Level.SEVERE, "YakClient had to quit unexpectedly because : ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }
}

private const val SETTINGS_NAME = "settings.conf"

public fun main(args: Array<String>) {
    val parser = ArgParser("yakclient")

    val yakDirectory by parser.option(FileArgument, "yakdirectory", "d").required()

    registerCustomType(UriCustomType())

    YakClient::class.java.module.addReads(ModuleLayer.boot().findModule("com.fasterxml.jackson.dataformat.xml").get())

    parser.parse(args).run {
        val settings : BootSettings = ConfigFactory.parseFile(yakDirectory.child(SETTINGS_NAME)).extract("boot")

        YakClient.yakDir = yakDirectory
        YakClient.settings = settings
        YakClient.init(
            ClassLoader.getSystemClassLoader(),
            settings,
            null
        )
    }

    val handler = RepositoryFactory.create(RepositorySettings(RepositoryType.MAVEN_CENTRAL, null, null))

    val dep = handler.find("net.yakclient:mixins-api:1.1.2")

    println(dep?.uri?.path)

    ExtensionLoader.load(ExtensionLoader.find(YakClient.settings.apiLocation), YakClient).onLoad()
}

