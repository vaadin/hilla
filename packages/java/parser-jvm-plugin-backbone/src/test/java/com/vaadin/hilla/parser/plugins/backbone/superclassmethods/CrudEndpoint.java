package com.vaadin.hilla.parser.plugins.backbone.superclassmethods;

import com.vaadin.hilla.EndpointExposed;

@EndpointExposed
public abstract class CrudEndpoint<T, ID> extends ReadOnlyEndpoint<T, ID> {
    public void delete(ID id) {
    }

    public T update(T entity) {
        return entity;
    }
}
