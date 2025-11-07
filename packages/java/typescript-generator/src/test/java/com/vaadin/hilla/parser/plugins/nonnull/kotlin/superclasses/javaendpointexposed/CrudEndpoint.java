package com.vaadin.hilla.parser.plugins.nonnull.kotlin.superclasses.javaendpointexposed;

import com.vaadin.hilla.parser.testutils.annotations.EndpointExposed;

@EndpointExposed
public abstract class CrudEndpoint<T, ID> extends ReadOnlyEndpoint<T, ID> {
    public void delete(ID id) {
    }

    public T update(T entity) {
        return entity;
    }
}
