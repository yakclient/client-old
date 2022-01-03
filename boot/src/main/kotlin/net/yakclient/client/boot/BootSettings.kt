package net.yakclient.client.boot

import java.io.File
import java.net.URI

public data class BootSettings(
    val mcVersion: String,
    val apiVersion: String,
    val mcExtLocation: URI,
    val mcLocation: URI,
    val apiLocation: URI,
    val apiInternalLocation: URI,
    val extensionDir: File
)
