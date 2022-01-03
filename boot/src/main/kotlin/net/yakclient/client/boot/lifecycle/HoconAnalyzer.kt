package net.yakclient.client.boot.lifecycle

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import net.yakclient.client.boot.setting.ExtensionSettings
import net.yakclient.client.boot.setting.SettingsAnalyzer
import java.io.InputStreamReader

public inline fun <reified T: ExtensionSettings> hoconAnalyzer() : SettingsAnalyzer<T> = SettingsAnalyzer(interpreter = {
    ConfigFactory.parseReader(InputStreamReader(it.asInputStream())).extract()
})