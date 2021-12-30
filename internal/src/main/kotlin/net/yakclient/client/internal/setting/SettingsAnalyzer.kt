package net.yakclient.client.internal.setting

import net.yakclient.client.internal.extension.AnalyzedExtReference
import net.yakclient.client.internal.extension.ExtensionAnalyzer
import net.yakclient.client.internal.extension.ExtensionEntry
import net.yakclient.client.internal.extension.ExtensionReference
import java.util.function.Function
import java.util.function.Predicate

private const val SETTINGS_FILE_NAME = "ext-settings.conf"

public typealias SettingsInterpreter = Function<ExtensionEntry, ExtensionSettings>

public typealias SettingsEntryMatcher = Predicate<ExtensionEntry>

public class SettingsAnalyzer(
    private val matcher: SettingsEntryMatcher = SettingsEntryMatcher { entry -> entry.name == SETTINGS_FILE_NAME },
    private val interpreter: SettingsInterpreter
) : ExtensionAnalyzer {
    override fun process(toProcess: ExtensionReference): AnalyzedExtReference =
        ExtSettingsReference(interpreter.apply(toProcess.values.find { matcher.test(it) }
            ?: throw IllegalStateException("Failed to find settings file in reference.")), toProcess)
}

public class ExtSettingsReference<T : ExtensionSettings>(
    public val settings: T,
    entries: Map<String, ExtensionEntry>
) : AnalyzedExtReference(entries)