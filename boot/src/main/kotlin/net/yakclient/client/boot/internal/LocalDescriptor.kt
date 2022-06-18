package net.yakclient.client.boot.internal

import net.yakclient.client.boot.dependency.Dependency

public data class LocalDescriptor(
    override val artifact: String,
    override val version: String?, override val classifier: String?
) : Dependency.Descriptor