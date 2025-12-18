package com.vaadin.hilla.parser.plugins.nonnull.kotlin.superclasses.javaendpointexposed

import com.vaadin.hilla.parser.testutils.annotations.Endpoint

@Endpoint
class PersonEndpoint : CrudEndpoint<PersonEndpoint.Person, Int>(), PagedData<PersonEndpoint.Person> {

    class Person {
        var name: String? = null
    }

    fun create(entity: Person): Int {
        return 0
    }
}
