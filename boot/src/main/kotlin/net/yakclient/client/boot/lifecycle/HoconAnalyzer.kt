package net.yakclient.client.boot.lifecycle

//public const val LOADER_SETTINGS_PATH : String = "loader"
//
//public inline fun <reified T: ExtensionSettings> hoconAnalyzer(path: String = LOADER_SETTINGS_PATH) : SettingsAnalyzer<T> = SettingsAnalyzer(interpreter = {
//    ConfigFactory.parseReader(InputStreamReader(it.asInputStream())).let { c -> if (path.isEmpty()) c.extract() else c.extract(path) }
//})