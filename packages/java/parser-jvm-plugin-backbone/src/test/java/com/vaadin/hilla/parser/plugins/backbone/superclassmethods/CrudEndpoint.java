package com.vaadin.hilla.parser.plugins.backbone.superclassmethods;

@EndpointExposed
public abstract class CrudEndpoint<T, ID> extends ReadOnlyEndpoint<T, ID> {
    public void delete(ID id) {
    }

    public T update(T entity) {
        return entity;
    }
}
