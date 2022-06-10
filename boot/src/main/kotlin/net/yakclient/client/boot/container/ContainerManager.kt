@file:JvmName("ContainerManager")

package net.yakclient.client.boot.container

import java.security.AccessController
import java.security.PrivilegedAction

private val walker = AccessController.doPrivileged(PrivilegedAction {
    StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
})

public fun callerContainer(): Container? = AccessController.doPrivileged(PrivilegedAction {
    walker.walk { stacks ->
        val containerStack: StackWalker.StackFrame? = stacks
            .filter { it.declaringClass.protectionDomain.codeSource is ContainerSource }
            .findFirst().orElse(null)

        val containerClass = containerStack?.declaringClass
        val containerSource = containerClass?.protectionDomain?.codeSource as? ContainerSource

        containerSource?.handle
    }?.handle
})
