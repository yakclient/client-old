package net.yakclient.client.internal.extension

public interface ExtensionAnalyzer : ExtLoadingProcess<ExtensionReference, AnalyzedExtReference> {
    override val accepts: Class<ExtensionReference>
        get() = ExtensionReference::class.java
}

public open class AnalyzedExtReference(
    entries: Map<String, ExtensionEntry>
) : ExtensionReference(entries)