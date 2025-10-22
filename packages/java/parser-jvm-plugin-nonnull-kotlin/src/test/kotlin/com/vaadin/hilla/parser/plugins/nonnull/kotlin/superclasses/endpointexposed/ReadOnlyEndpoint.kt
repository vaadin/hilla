package com.vaadin.hilla.parser.plugins.nonnull.kotlin.superclasses.endpointexposed

import com.vaadin.hilla.parser.plugins.nonnull.kotlin.annotation.EndpointExposed
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.superclasses.javaendpointexposed.NonEndpoint
import com.vaadin.hilla.parser.plugins.nonnull.kotlin.superclasses.javaendpointexposed.NonEndpointImpl

@EndpointExposed
open class ReadOnlyEndpoint<T, ID> : NonEndpointImpl(), NonEndpoint {
    fun get(id: ID): T? = null
}
