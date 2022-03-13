package net.yakclient.client.boot.schema

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

// TODO possibly make a way to ignore values or have values not need to be present yet. For example in the maven schema the jar and pom dont need to be present if a version isn't defined in the context.
public interface Schema<C : Schema.Context> {
    public val handler: SchemaHandler<C>

    //    public val tiers: Tiers
    public val contextHandle: ContextHandler<C>
        get() = ContextHandler(handler)

    public interface Context

//    public fun validate(c: C): ContextHandler<C>? {
//        if (!handler.validate(c)) return null
//        return ContextHandler(c)
//    }

//    public fun

//    public interface Tier<S : Schema, T : Context> : (T) -> Boolean {
//        public val handler: TierHandler<S, T>
//        public val type: KClass<T>
//
//
//    }


//    public interface Tier<C: TierContext, S: Tier<C, S, N>, N: Tier<*, EndTier, EndTier>> {
//        public fun <T> createElement(provider: SchemeProvider<C, T>) : SchemeProviderDelegate<C, T> = TODO()
//
//        public fun progress(any: TierContext): Nothing = TODO()
//    }


//    public fun <C: Context, T> createScheme(provider: SchemeProvider<C, T>): SchemeProviderDelegate<C, T> =
//        handler.registerScheme(provider)
}

//public class Tiers<S : Schema> internal constructor() : MutableList<Schema.Tier<S, *>> by ArrayList() {
//    protected fun tierOf(tier: Schema.Tier<S, *>): Tiers<S> = apply { add(tier) }

//    protected inline fun <reified T : Schema.Context> tierOf(crossinline validator: (T) -> Boolean): Tiers<S> =
//        tierOf(object : Schema.Tier<S, T> {
//            override val type: KClass<T> = T::class
//            override fun invoke(p1: T): Boolean = validator(p1)
//        })
//}


//public class TierValidator(
//    private val tiers: List<Schema.Tier<*>>,
//)

//public abstract class Start<Self: Start<Self, T>, T: End<T, Self>> {
//    public lateinit var end: T
//}
//
//public class asdf : Start<asdf, End<asddsaf, asdf>>()
//
//public class asddsaf : Start<asddsaf, endasdf>()
//
//public abstract class End<Self: End<Self, T>, T: Start<T, Self>> {
//    public lateinit var start: T
//}
//
//public class endasdf : End<endasdf, asddsaf>()


//public class EndTier private constructor(): Schema.Tier<EndTier, EndTier>

//public fun <C: Schema.Context,R> Schema<C>.validate(c: C, block: ContextHandler<C>.() -> R): R? = validate(c)?.run(block)

//public class SchemeProviderDelegate<C : Schema.Context, T>(
//    private val provider: ValueProvider<C, T>
//) {
//    public operator fun getValue(thisRef: Schema.Tier<out Schema<*>, C>, property: KProperty<*>): ValueProvider<C, T> =
//        provider
//}

public fun interface ValueProvider<C : Schema.Context, T> : (C) -> T
public fun interface ContextValidator<C : Schema.Context> : (C) -> Boolean

public class SchemaHandler<C : Schema.Context> {
    internal val validators: MutableMap<String, ContextValidator<*>> = HashMap()
//    internal val schemes: MutableMap<String, ValueProvider<*, *>> = LinkedHashMap()
//    internal val tiers: Tiers<S> = Tiers()

//    internal fun <C : Schema.Context, T> registerScheme(
//        rep: KProperty1<S, T>,
//        provider: ValueProvider<C, T>
//    ): SchemeProviderDelegate<C, T> =
//        provider.also { schemes[rep.name] = provider }.let(::SchemeProviderDelegate)
//}

    //public class TierHandler<S : Schema, C : Schema.Context> {
//    private val schemes: MutableMap<String, ValueProvider<C, *>> = HashMap()
//
    public fun <T, NC : C> register(
        type: KClass<NC>, provider: ValueProvider<NC, T>
    ): SchemaMeta<NC, T> = SchemaMeta(provider, type)
//        provider.also { schemes[rep.name] = provider }.let(::SchemeProviderDelegate)

    public fun <NC : C> registerValidator(
        type: KClass<NC>,
        validator: ContextValidator<NC>
    ) {
        validators[type.java.name] = validator
    }

    public inline fun <reified NC: C> registerValidator(
        validator: ContextValidator<NC>
    ): Unit = registerValidator(NC::class, validator)

    public inline fun <reified NC : C, T> register(
        provider: ValueProvider<NC, T>
    ): SchemaMeta<NC, T> = register(NC::class, provider)
}

public data class SchemaMeta<C : Schema.Context, T>(
    internal val provider: ValueProvider<C, T>, internal val contextType: KClass<C>
)

public class SchemaPropertyDelegate<T>(
    private val meta: SchemaMeta<*, T>, private val context: ContextHandler<*>
) {
    public operator fun getValue(ref: Nothing?, prop: KProperty<*>): T = context.getValue(meta)
//        val currentContext = context._context ?: throw IllegalStateException("No context provided!")
//
//        check(meta.contextType.java.isAssignableFrom(currentContext::class.java)) { "Illegal querying of property: ${prop.name} when invalid context is provided. Provided: ${currentContext::class.qualifiedName} required ${meta.contextType.qualifiedName}" }
//
//        return (meta.provider as ValueProvider<Schema.Context, T>).provide(currentContext)

}

public class ContextHandler<C : Schema.Context>(
    private val handler: SchemaHandler<C>,
//    initial: Schema.Context
) {
    //    private val contexts: MutableList<Schema.Context> = ArrayList()
    private var _context: C? = null
        set(value) {
            checkNotNull(value)
            assert((handler.validators[value::class.java.name]
                    as? ContextValidator<Schema.Context>)?.invoke(value) ?: true) {"Invalid context: '$value'! Failed to pass validation checks!"}

            field = value
            context = value
        }
    public lateinit var context: C


    public fun <C : Schema.Context, T> getValue(
//        rep: KProperty1<out Schema, ValueProvider<C, T>>,
//        contextType: KClass<C>
        meta: SchemaMeta<C, T>
    ): T {
        val currentContext = _context ?: throw IllegalStateException("No context provided!")

        check(meta.contextType.java.isAssignableFrom(currentContext::class.java)) { "Invalid context: $currentContext. Needed ${meta.contextType.qualifiedName} provided ${currentContext::class.qualifiedName}" }

        return (meta.provider as ValueProvider<Schema.Context, T>)(currentContext)
    }

    public operator fun <C : Schema.Context, T> get(
//        rep: KProperty1<out Schema, ValueProvider<C, T>>,
//        contextType: KClass<C>
        meta: SchemaMeta<C, T>
    ): SchemaPropertyDelegate<T> = SchemaPropertyDelegate(meta, this)
//        (handler.schemes[rep.name]
//            ?: throw IllegalArgumentException("Invalid Property: '${rep.name}'. Failed to find it in the schema."))

    //
//        public


    public fun supply(c: C) : ContextHandler<C> {
        _context = c
        return this
    }
//    public fun <T> KProperty1<out Schema, *>.get() : T = get(this@ContextHandler)
}

//public fun <C : Schema.Context, T : Any> SchemeHan/*dler.createScheme(
//    tier: Schema.Tier<*>,
//    provider: ValueProvider<C, T>
//): SchemeProviderDelegate<C, T> = registerScheme(tier, provider)*/