package net.yakclient.client.util

import kotlin.reflect.KProperty1

public class MutablePropertySet<E>(
    vararg properties: KProperty1<E, *>,
    private val delegate: MutableSet<E> = HashSet()
) : MutableSet<E> by delegate {
    private val properties: List<KProperty1<E, *>> = properties.toList()
    private val mapping: Map<String, MutableMap<Any, E>> = buildMap<String, MutableMap<Any, E>> {
        properties.forEach {
            put(it.name, HashMap())
        }
    }

    public fun <T : Any> getBy(property: KProperty1<E, T>, value: T): E? = (mapping[property.name]
        ?: throw IllegalArgumentException("Property ${property.name} not initiated on construction!"))[value]

    public fun <T : Any> containsBy(property: KProperty1<E, T>, value: T): Boolean = (mapping[property.name]
        ?: throw IllegalArgumentException("Property ${property.name} not initiated on construction!")).containsKey(value)

    override fun add(element: E): Boolean {
        return !delegate.add(element).also { b ->
            if (!b) {
                properties.map { it.name to it.get(element) }.forEach {
                    if (it.second != null) mapping[it.first]!![it.second!!] = element
                }
            }
        }
    }
}