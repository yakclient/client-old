package net.yakclient.client.boot

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ParsingException
import kotlinx.cli.required
import net.yakclient.client.internal.setting.YakLaunchSettings
import java.io.File
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public object YakClient {
    public var settings: YakLaunchSettings by object : ReadWriteProperty<YakClient, YakLaunchSettings> {
        private lateinit var value: YakLaunchSettings

        override fun getValue(thisRef: YakClient, property: KProperty<*>): YakLaunchSettings = value

        override fun setValue(thisRef: YakClient, property: KProperty<*>, value: YakLaunchSettings) =
            if (this::value.isInitialized)
                throw UnsupportedOperationException("Cannot set launch settings at this moment")
            else this.value = value
    }
}

public fun main(args: Array<String>) {
    val fileType = object : ArgType<File>(false) {
        override val description: kotlin.String = "{ java.nio.File }"

        override fun convert(value: kotlin.String, name: kotlin.String): File =
            File(value).takeIf { it.exists() }
                ?: throw ParsingException("Option $name expected to be an existing file path. $value is provided.")
    }

    val parser = ArgParser("yakclient")

    val mcVersion by parser.option(ArgType.String, "mcversion", "mcv").required()
    val apiVersion by parser.option(ArgType.String, "apiversion", "apiv").required()
    val mcLocation by parser.option(fileType, "mclocation", "mcl").required()
    val apiLocation by parser.option(fileType, "apilocation", "apil").required()
    val extensionDir by parser.option(fileType, "extensiondirectory", "extd").required()

    parser.parse(args).run {

    }
}

