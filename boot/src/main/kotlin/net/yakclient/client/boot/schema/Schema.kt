package net.yakclient.client.boot.schema

import net.yakclient.client.boot.exception.SchemeNotCreatedException
import kotlin.reflect.KProperty

// TODO possibly make a way to ignore values or have values not need to be present yet. For example in the maven schema the jar and pom dont need to be present if a version isn't defined in the context.
public interface Schema<C : Schema.Context> {
    public val handler: SchemeHandler<C>

    public interface Context

    public fun validate(c: C): ContextHandler<C>? {
        if (!handler.validate(c)) return null
        return ContextHandler(c)
    }

    public fun <T> createScheme(provider: SchemeProvider<C, T>): SchemeProviderDelegate<C, T> =
        handler.registerScheme(provider)
}

public fun <C: Schema.Context,R> Schema<C>.validate(c: C, block: ContextHandler<C>.() -> R): R? = validate(c)?.run(block)

public class SchemeProviderDelegate<C : Schema.Context, T>(
    private val provider: SchemeProvider<C, T>
) {
    public operator fun getValue(thisRef: Schema<C>, property: KProperty<*>): SchemeProvider<C, T> = provider
}

public fun interface SchemeProvider<C : Schema.Context, T> {
    public fun provide(c: C): T

    public fun validate(c: C): Boolean = runCatching {
        provide(c)
    }.isSuccess
}

public class SchemeHandler<C : Schema.Context> {
    private val schemes: MutableList<SchemeProvider<C, *>> = ArrayList()

    internal fun <T> registerScheme(provider: SchemeProvider<C, T>): SchemeProviderDelegate<C, T> =
        SchemeProviderDelegate(provider).also { schemes.add(provider) }

    public fun validate(c: C): Boolean = schemes.all { it.validate(c) }
}

public class ContextHandler<C : Schema.Context>(
    private val c: C
) {
    public fun <T> get(provider: SchemeProvider<C, T>): T = provider.provide(c)
}

//public fun <C : Schema.Context, T : Any> Schema<C>.createScheme(provider: SchemeProvider<C, T>): SchemeProviderDelegate<C, T> =
//    SchemeProviderDelegate(provider)

public fun <C : Schema.Context, T> Schema<C>.abstractScheme(): SchemeProviderDelegate<C, T> =
    SchemeProviderDelegate { throw SchemeNotCreatedException(this::class.java.name) }


