package net.yakclient.client.boot

import net.yakclient.client.boot.container.ContainerSource
import java.security.*


internal class BasicPolicy : Policy() {
    private val permissions: PermissionCollection = Permissions()
    init {
        permissions.add(AllPermission())
    }

    override fun getPermissions(codesource: CodeSource): PermissionCollection? = if (codesource !is ContainerSource) permissions else null
}