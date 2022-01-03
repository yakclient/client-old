package net.yakclient.client.boot.ext

public fun interface ExtensionAnalyzer : ExtLoadingProcess<ExtensionReference, AnalyzedExtReference>

public open class AnalyzedExtReference(
    entries: Map<String, ExtensionEntry>
) : ExtensionReference(entries)