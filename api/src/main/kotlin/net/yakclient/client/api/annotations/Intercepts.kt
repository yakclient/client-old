package net.yakclient.client.api.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
public annotation class Intercepts(
    public val packets: Array<KClass<*>> = [],
    public val priority: Int = 0
)
