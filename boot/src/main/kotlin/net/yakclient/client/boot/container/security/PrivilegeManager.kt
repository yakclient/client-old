package net.yakclient.client.boot.container.security

import net.yakclient.client.boot.container.Container
import net.yakclient.client.boot.container.callerContainer

public object PrivilegeManager {
    public fun hasPrivilege(container: Container, privilege: ContainerPrivilege): Boolean {
        return container.privileges.any { it.name == privilege.name || it.implies(privilege) }
    }

    public fun hasPrivilege(privilege: ContainerPrivilege): Boolean {
        return callerContainer()?.let { hasPrivilege(it, privilege) } ?: true
    }

    public fun createPrivileges(vararg privileges: ContainerPrivilege) : Privileges {
        if (privileges.isEmpty()) return PrivilegeList(listOf())

        return createPrivileges(privileges.toList())
    }

    public fun createPrivileges(privileges: List<ContainerPrivilege>) : Privileges {
        check(privileges.all(::hasPrivilege)) { "Insufficient privileges to create: ${privileges.joinToString { it.name }}" }

        return PrivilegeList(privileges)
    }

    public fun allPrivileges(): Privileges {
        val privilege = AllPrivilege()
        if (!hasPrivilege(privilege)) throw SecurityException("Insufficient privileges to create the all privileges!")

        return PrivilegeList(listOf(privilege))
    }

    public fun Privileges.toMutablePrivileges() : MutablePrivilegeList = MutablePrivilegeList(toMutableList())
}

