package net.yakclient.client.boot.repository

import java.net.URI

public data class RepositorySettings(
    val type: RepositoryType,
    val location: URI,
    val name: String?
)
