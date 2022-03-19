package net.yakclient.client.util.test

import net.yakclient.client.util.Schema
import net.yakclient.client.util.SchemaHandler
import net.yakclient.client.util.SchemaMeta
import kotlin.reflect.KClass
import kotlin.test.Test

class SchemaTests {
    @Test
    fun `Test Person Schema`() {
        val schema = PersonSchemaImpl(SchemaHandler())

        val handle = schema.contextHandle

        val name by handle[schema.name]
        val fullName by handle[schema.fullName]
        val lastName by handle[schema.lastName]

        assert(runCatching {
            println(name) // Should throw
        }.isFailure)

        handle.supply(Person("Bobbie"))
        println(name)

        assert(runCatching {
            println(fullName) // Should throw
            println(lastName) // Unreachable
        }.isFailure)

        handle.supply(QualifiedPerson(handle.context, "Davidson"))
        println(fullName)
        println(lastName)
    }
}

interface PersonSchema : Schema<Person> {
    val name: SchemaMeta<Person, String>
    val lastName: SchemaMeta<QualifiedPerson, String>
    val fullName: SchemaMeta<QualifiedPerson, String>
}

class PersonSchemaImpl(override val handler: SchemaHandler<Person>) : PersonSchema {
    init {
        handler.registerValidator<Person> {
            println("Validating non-qualified")
            it.name == "Bobbie"
        }

        handler.registerValidator<QualifiedPerson> {
            println("Validating Qualified")
            true
        }
    }

    override val name = handler.register(Person::class, Person::name)
    override val lastName = handler.register(QualifiedPerson::class, QualifiedPerson::lastName)
    override val fullName = handler.register(QualifiedPerson::class) { "${it.name} ${it.lastName}" }
}

open class Person(
    val name: String
) : Schema.Context

class QualifiedPerson(
    name: String,
    val lastName: String
) : Person(name) {
    constructor(p: Person, lastName: String) : this(p.name, lastName)
}

