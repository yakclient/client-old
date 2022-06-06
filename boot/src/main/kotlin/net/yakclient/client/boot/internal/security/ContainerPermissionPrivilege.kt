package net.yakclient.client.boot.internal.security

import net.yakclient.client.boot.container.security.ContainerPrivilege
import net.yakclient.common.util.runCatching
import java.security.Permission

internal data class ContainerPermissionPrivilege(
    val permission: Permission
) : ContainerPrivilege {
    override val name: String = permission.name

    override fun checkAccess(o: Any): Boolean =
        runCatching(SecurityException::class) { permission.checkGuard(o) } != null

    override fun implies(other: ContainerPrivilege): Boolean = if (other is ContainerPermissionPrivilege) permission.implies(other.permission) else false
}