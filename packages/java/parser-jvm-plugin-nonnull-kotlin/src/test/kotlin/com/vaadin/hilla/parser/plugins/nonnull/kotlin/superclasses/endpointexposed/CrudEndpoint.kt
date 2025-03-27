package com.vaadin.hilla.parser.plugins.nonnull.kotlin.superclasses.endpointexposed

import com.vaadin.hilla.parser.plugins.nonnull.kotlin.annotation.EndpointExposed

@EndpointExposed
abstract class CrudEndpoint<T, ID> : ReadOnlyEndpoint<T, ID>() {

    fun delete(id: ID) = Unit

    fun update(entity: T): T {
        return entity
    }

    fun save(entity: T?): T? {
        return entity
    }
}
