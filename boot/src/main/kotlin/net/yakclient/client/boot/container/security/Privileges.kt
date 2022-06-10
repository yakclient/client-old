package net.yakclient.client.boot.container.security

import net.yakclient.client.boot.container.security.ContainerPermissionPrivilege
import net.yakclient.common.util.UNUSED_INLINE
import java.security.Permission
import java.security.PermissionCollection
import java.security.Permissions

//public typealias PrivilegeList = List<ContainerPrivilege>

public sealed class Privileges (
    delegate: List<ContainerPrivilege>
) : List<ContainerPrivilege> by delegate

public class PrivilegeList internal constructor(delegate: List<ContainerPrivilege>) : Privileges(delegate)

public class MutablePrivilegeList internal constructor(
    private val delegate: MutableList<ContainerPrivilege>
) : Privileges(delegate) {
    public fun add(privilege: ContainerPrivilege) {
        if (!PrivilegeManager.hasPrivilege(privilege)) throw SecurityException("Cannot add: ${privilege.name} to privileges as caller does not have sufficient privileges!")

        delegate.add(privilege)
    }
}

@JvmName("toPermissionCollectionExtension")
@Suppress(UNUSED_INLINE)
public inline fun Privileges.toPermissionCollection(): PermissionCollection = toPermissionCollection(this)

public fun Permission.toPrivilege(): ContainerPrivilege = ContainerPermissionPrivilege(this)

public fun toPermissionCollection(list: Privileges): PermissionCollection = Permissions().apply {
    class JavaPermissionWrapper(
        val privilege: ContainerPrivilege
    ) : Permission(privilege.name) {
        override fun equals(other: Any?): Boolean = privilege == other

        override fun hashCode(): Int = privilege.hashCode()

        override fun implies(permission: Permission): Boolean = false

        override fun getActions(): String = name

        override fun checkGuard(`object`: Any): Unit =
            if (!privilege.checkAccess(`object`)) throw SecurityException("Access to privilege: '$name' denied to '$`object`'") else Unit
    }

    list.forEach {
        add(if (it is ContainerPermissionPrivilege) it.permission else JavaPermissionWrapper(it))
    }
}