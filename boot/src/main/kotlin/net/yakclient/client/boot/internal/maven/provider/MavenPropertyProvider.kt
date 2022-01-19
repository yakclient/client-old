package net.yakclient.client.boot.internal.maven.provider

import org.w3c.dom.Element

internal interface MavenPropertyProvider {
    fun provide(document: Element, property:String) : String?
}