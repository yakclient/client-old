package net.yakclient.client.boot.internal.maven.property

import net.yakclient.client.boot.internal.maven.Pom

internal interface MavenPropertyProvider {
    fun provide(pom: Pom, property:String) : String?
}