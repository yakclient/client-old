package net.yakclient.client.boot.repository

import java.net.URI

public data class Dependency(
    val uri: URI,
    val dependants: List<Dependency>
)
