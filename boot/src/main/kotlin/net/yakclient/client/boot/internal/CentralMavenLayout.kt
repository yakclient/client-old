package net.yakclient.client.boot.internal

import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.maven.DEFAULT_MAVEN_LAYOUT
import net.yakclient.client.boot.maven.LAYOUT_OPTION_NAME
import net.yakclient.client.boot.maven.URL_OPTION_NAME
import net.yakclient.client.boot.repository.RepositorySettings

internal object CentralMavenLayout : DefaultMavenLayout(RepositorySettings(MAVEN_CENTRAL, mapOf(URL_OPTION_NAME to mavenCentral, LAYOUT_OPTION_NAME to DEFAULT_MAVEN_LAYOUT)))