package net.yakclient.client.boot.repository

public data class RepositorySettings(
    val type: String,
    val options: Map<String, String> = mapOf()
)
