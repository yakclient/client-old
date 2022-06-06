package net.yakclient.client.boot.loader

import net.yakclient.client.boot.container.security.PrivilegeList
import net.yakclient.client.boot.container.security.toPermissionCollection
import net.yakclient.client.boot.container.ContainerSource
import java.nio.ByteBuffer
import java.security.ProtectionDomain

public class ContainerClassLoader(
    sourceProvider: SourceProvider,
    privileges: PrivilegeList,
    source: ContainerSource,
    _components: List<ClComponent>,
    parent: ClassLoader,
) : ProvidedClassLoader(
    sourceProvider,
    _components,
    parent
) {
    private val domain: ProtectionDomain =
        ProtectionDomain(source, privileges.toPermissionCollection(), this, null)

    override fun loadLocalClass(name: String, buffer: ByteBuffer): Class<*> = defineClass(name, buffer, domain)
}