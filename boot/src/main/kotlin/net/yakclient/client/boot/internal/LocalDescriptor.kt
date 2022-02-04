package net.yakclient.client.boot.internal

import net.yakclient.client.boot.dep.Dependency

public data class LocalDescriptor(
    override val artifact: String,
    override val version: String?
) : Dependency.Descriptor