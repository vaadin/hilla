package com.vaadin.hilla.crud;

/**
 * A browser-callable service that can create, read, update, and delete a given
 * type of object.
 *
 * @deprecated Use {@link com.vaadin.flow.spring.data.CrudService} instead
 */
@Deprecated(forRemoval = true)
public interface CrudService<T, ID>
        extends com.vaadin.flow.spring.data.CrudService<T, ID> {
}
