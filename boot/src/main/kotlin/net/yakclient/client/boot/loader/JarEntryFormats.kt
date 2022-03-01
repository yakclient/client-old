package net.yakclient.client.boot.loader

internal val String.packageFormat get(): String = substring(0, lastIndexOf('.'))

internal val String.dotClassFormat get() : String = "${replace('.', '/')}.class"