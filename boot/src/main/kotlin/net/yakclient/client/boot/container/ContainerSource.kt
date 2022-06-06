package net.yakclient.client.boot.container

import java.security.CodeSource
import java.security.cert.Certificate

public class ContainerSource(
    public val handle: ContainerHandle
) : CodeSource(null, arrayOf<Certificate>())