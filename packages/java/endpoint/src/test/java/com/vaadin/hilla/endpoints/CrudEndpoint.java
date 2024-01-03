package com.vaadin.hilla.endpoints;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import com.vaadin.hilla.EndpointExposed;

@EndpointExposed
@AnonymousAllowed
public abstract class CrudEndpoint<T, ID> extends ReadOnlyEndpoint<T, ID> {
    public void delete(ID id) {
    }

    public T update(T entity) {
        return entity;
    }
}
