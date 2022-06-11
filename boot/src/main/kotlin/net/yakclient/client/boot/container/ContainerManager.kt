@file:JvmName("ContainerManager")

package net.yakclient.client.boot.container

import java.security.AccessController
import java.security.PrivilegedAction

private val walker = AccessController.doPrivileged(PrivilegedAction {
    StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
})

public fun containerOf(cls: Class<*>) : Container? {
    val containerSource = cls.protectionDomain.codeSource as? ContainerSource

    return containerSource?.handle?.handle
}

public fun callerContainer(): Container? = AccessController.doPrivileged(PrivilegedAction {
    walker.walk { stacks ->
        val containerStack: StackWalker.StackFrame? = stacks
            .filter { it.declaringClass.protectionDomain.codeSource is ContainerSource }
            .findFirst().orElse(null)

        containerStack?.declaringClass?.let(::containerOf)
    }
})
