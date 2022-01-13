package net.yakclient.client.boot.ext

import java.net.URI

public abstract class ExtReference(
    delegate: Map<String, URI>
) : Map<String, URI> by delegate