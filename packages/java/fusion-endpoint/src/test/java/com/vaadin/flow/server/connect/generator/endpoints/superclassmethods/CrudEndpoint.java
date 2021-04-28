package com.vaadin.flow.server.connect.generator.endpoints.superclassmethods;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.connect.EndpointExposed;

@EndpointExposed
@AnonymousAllowed
public abstract class CrudEndpoint<T, ID> extends ReadOnlyEndpoint<T, ID> {
    public T update(T entity) {
        return entity;
    }

    public void delete(ID id) {
    }
}
