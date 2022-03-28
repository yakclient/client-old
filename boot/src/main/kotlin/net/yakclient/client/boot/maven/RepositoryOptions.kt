package net.yakclient.client.boot.maven

import net.yakclient.client.boot.repository.RepositorySettings

internal const val LAYOUT_OPTION_NAME = "layout"
internal const val DEFAULT_MAVEN_LAYOUT = "default"
internal const val SNAPSHOT_MAVEN_LAYOUT = "snapshot"
internal const val URL_OPTION_NAME = "url"

internal val RepositorySettings.layout : String
    get() = options[LAYOUT_OPTION_NAME] ?: DEFAULT_MAVEN_LAYOUT

internal val RepositorySettings.url: String?
    get() = options[URL_OPTION_NAME]