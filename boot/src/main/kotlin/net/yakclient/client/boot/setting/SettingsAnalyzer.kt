package net.yakclient.client.boot.setting

import net.yakclient.client.boot.ext.*
import java.net.URI
import java.util.function.BiPredicate
import java.util.function.Function
import java.util.function.Predicate

//private const val SETTINGS_FILE_NAME = "ext-settings.conf"
//
//public fun interface SettingsInterpreter<T: ExtensionSettings> : Function<URI, T>
//
//public typealias SettingsEntryMatcher = BiPredicate<String, URI>
//
//public class SettingsAnalyzer<T: ExtensionSettings>(
//    private val matcher: SettingsEntryMatcher = SettingsEntryMatcher { name, _ -> name == SETTINGS_FILE_NAME },
//    private val interpreter: SettingsInterpreter<T>
//) : ExtLoadingProcess<ExtReference, ExtSettingsReference<T>> {
//    override fun process(toProcess: ExtReference): ExtSettingsReference<T> =
//        ExtSettingsReference(interpreter.apply(toProcess.values.find { matcher.test(it) }
//            ?: throw IllegalStateException("Failed to find settings file in reference.")), toProcess)
//}
//
//public class ExtSettingsReference<out T : ExtensionSettings>(
//    public val settings: T,
//    entries: Map<String, ExtensionEntry>
//) : ExtensionReference(entries)