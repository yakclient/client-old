package net.yakclient.client.boot.container.security

import net.yakclient.client.boot.internal.security.ContainerPermissionPrivilege
import java.security.Permission

public fun Permission.toPrivilege() : ContainerPrivilege = ContainerPermissionPrivilege(this)

public fun allPrivileges() : PrivilegeList = listOf(object : ContainerPrivilege {
    override val name: String = "all"

    override fun checkAccess(o: Any): Boolean {
        TODO("Not yet implemented")
    }

    override fun implies(other: ContainerPrivilege): Boolean = true
})
