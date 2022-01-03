package net.yakclient.client.boot.setting

import net.yakclient.client.boot.ext.*
import java.util.function.Function
import java.util.function.Predicate

private const val SETTINGS_FILE_NAME = "ext-settings.conf"

public fun interface SettingsInterpreter<T: ExtensionSettings> : Function<ExtensionEntry, T>

public typealias SettingsEntryMatcher = Predicate<ExtensionEntry>

public class SettingsAnalyzer<T: ExtensionSettings>(
    private val matcher: SettingsEntryMatcher = SettingsEntryMatcher { entry -> entry.name == SETTINGS_FILE_NAME },
    private val interpreter: SettingsInterpreter<T>
) : ExtLoadingProcess<ExtensionReference, ExtSettingsReference<T>> {
    override fun process(toProcess: ExtensionReference): ExtSettingsReference<T> =
        ExtSettingsReference(interpreter.apply(toProcess.values.find { matcher.test(it) }
            ?: throw IllegalStateException("Failed to find settings file in reference.")), toProcess)
}

public class ExtSettingsReference<out T : ExtensionSettings>(
    public val settings: T,
    entries: Map<String, ExtensionEntry>
) : AnalyzedExtReference(entries)