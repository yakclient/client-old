package net.yakclient.client.util

import org.w3c.dom.Element
import org.w3c.dom.Node


public operator fun Element.get(tag: String): List<Node> =  ArrayList<Node>().apply {
    val list = getElementsByTagName(tag)

    for (i in 0 until list.length) {
        add(list.item(i))
    }
}

public operator fun Node.get(tag: String) : List<Node> = ArrayList<Node>().apply {
    for (i in 0 until childNodes.length) {
        val item = childNodes.item(i)
        if (item.nodeName == tag) add(item)
    }
}

public fun Node.stringValue(tag: String): String? = this[tag].firstOrNull()?.childNodes?.item(0)?.nodeValue


//((builder.parse(pom.openStream()).also {
//    it.documentElement.normalize()
//}.documentElement.getElementsByTagName("dependencies").item(0).childNodes.item(1).childNodes.item(1).childNodes)).item(0).nodeValue