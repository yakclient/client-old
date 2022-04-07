package net.yakclient.client.boot.maven

import net.yakclient.client.boot.repository.RepositorySettings

public const val LAYOUT_OPTION_NAME: String = "layout"
public const val DEFAULT_MAVEN_LAYOUT: String = "default"
public const val SNAPSHOT_MAVEN_LAYOUT: String = "snapshot"
public const val URL_OPTION_NAME: String = "url"

public val RepositorySettings.layout : String
    get() = options[LAYOUT_OPTION_NAME] ?: DEFAULT_MAVEN_LAYOUT

public val RepositorySettings.url: String?
    get() = options[URL_OPTION_NAME]