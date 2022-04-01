package net.yakclient.client.util

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

public fun URL.isReachable(): Boolean = try {
    when (protocol) {
        "file" -> Files.exists(Path.of(file))
        else -> (openConnection() as? HttpURLConnection)?.responseCode == 200
    }
} catch (_: IOException) {
    false
}

public val URL.baseURL: String
    get() = this.protocol + ':' +
            (this.authority.takeUnless { it == null || it.isEmpty() }?.let { "//$it" } ?: "") +
            (this.path ?: "").removeSuffix("/")

public fun URL.uriAt(path: String): URI = URI(("$baseURL/$path"))
public fun URL.urlAt(vararg paths: String): URL = URL(paths.fold(baseURL) { acc, it -> "$acc/$it" })