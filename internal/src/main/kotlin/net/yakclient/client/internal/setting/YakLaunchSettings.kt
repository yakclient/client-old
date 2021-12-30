package net.yakclient.client.internal.setting

import java.net.URL

public data class YakLaunchSettings(
    val minecraftLocation: URL,
    val minecraftVersion: String,
    val apiLocation: URL,
    val apiVersion: String,
)
