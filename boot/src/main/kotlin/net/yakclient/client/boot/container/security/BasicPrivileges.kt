package net.yakclient.client.boot.container.security

import net.yakclient.common.util.runCatching
import java.io.FilePermission
import java.security.AllPermission
import java.security.Permission

public open class ContainerPermissionPrivilege(
    internal val permission: Permission
) : ContainerPrivilege {
    override val name: String = permission.name

    override fun checkAccess(o: Any): Boolean =
        runCatching(SecurityException::class) { permission.checkGuard(o) } != null

    override fun implies(other: ContainerPrivilege): Boolean =
        if (other is ContainerPermissionPrivilege) permission.implies(other.permission) else false
}

public class AllPrivilege : ContainerPermissionPrivilege(AllPermission()) {
    override fun implies(other: ContainerPrivilege): Boolean = true
}

public class FilePrivilege(path: String, action: List<FileAction>) : ContainerPermissionPrivilege(
    FilePermission(
        path,
        action.joinToString(separator = ",", transform = FileAction::internalName)
    )
) {
    public constructor(path: String, vararg actions : FileAction) : this(path, actions.toList())
}

public enum class FileAction(
    internal val internalName: String
) {
    READ("read"),
    WRITE("write"),
    DELETE("delete"),
    EXECUTE("execute"),
    READLINK("readlink")
}

