@file:JvmName("ContainerManager")

package net.yakclient.client.boot.container

public fun callerContainer(): Container? {
    return StackWalker.getInstance().walk { stacks ->
        val containerStack: StackWalker.StackFrame? = stacks
            .filter { it.declaringClass.protectionDomain.codeSource is ContainerSource }
            .findFirst().orElse(null)

        val containerClass = containerStack?.declaringClass
        val containerSource = containerClass?.protectionDomain?.codeSource as? ContainerSource

        containerSource?.handle
    }?.handle
}