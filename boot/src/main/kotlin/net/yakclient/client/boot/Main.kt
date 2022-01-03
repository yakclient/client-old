package net.yakclient.client.boot

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.github.config4k.registerCustomType
import kotlinx.cli.ArgParser
import kotlinx.cli.required
import net.yakclient.client.boot.ext.Extension
import net.yakclient.client.boot.lifecycle.loadJar
import net.yakclient.client.util.FileArgument
import net.yakclient.client.util.UriCustomType
import net.yakclient.client.util.child
import net.yakclient.client.util.immutableLateInit
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

public object YakClient {
    public var settings: BootSettings by immutableLateInit()
    public val sysLogger: Logger = Logger.getLogger("YakClient")
    internal val boot: Extension = BootModule(YakClient::class.java.classLoader)
    public var yakDir : File by immutableLateInit()

    public fun exit(e: Exception, extra: String = "A critical error has occurred"): Nothing {
        sysLogger.log(Level.SEVERE, extra)
        sysLogger.log(Level.SEVERE, "YakClient had to quit unexpectedly because : ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }
}

private const val SETTINGS_NAME = "settings.conf"

public fun main(args: Array<String>) {
    val parser = ArgParser("yakclient")

    val yakDirectory by parser.option(FileArgument, "yakdirectory", "d").required()

    registerCustomType(UriCustomType())

    parser.parse(args).run {
        YakClient.yakDir = yakDirectory
        YakClient.settings = ConfigFactory.parseFile(yakDirectory.child(SETTINGS_NAME)).extract("boot")
    }

    YakExtensionManager.extLoader(YakClient.boot).loadJar(YakClient.settings.apiLocation)

//    println(YakExtensionManager.minecraft)
}

