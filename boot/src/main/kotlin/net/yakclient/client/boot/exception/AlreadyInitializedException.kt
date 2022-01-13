package net.yakclient.client.boot.exception

import kotlin.reflect.KClass

internal class AlreadyInitializedException(clazz: KClass<*>) : Exception("Class ${clazz.qualifiedName} has already been initialized!")