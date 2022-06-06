package net.yakclient.client.boot.container.security

public interface ContainerPrivilege {
    public val name: String

    public fun checkAccess(o: Any) : Boolean

    public fun implies(other: ContainerPrivilege) : Boolean
}