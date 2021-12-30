package net.yakclient.client.internal.lifecycle

import com.typesafe.config.Config
import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import net.yakclient.client.internal.extension.ExtensionEntry
import net.yakclient.client.internal.setting.ExtensionSettings
import net.yakclient.client.internal.setting.SettingsInterpreter
import java.io.InputStreamReader


public class HoconSettingsInterpreter<T: ExtensionSettings>(
    private val settingsType: Class<T>
) : SettingsInterpreter {
    override fun apply(t: ExtensionEntry): T {
        val config: Config = ConfigFactory.parseReader(InputStreamReader(t.asInputStream()))
        return ConfigBeanFactory.create(config, settingsType)
    }
}