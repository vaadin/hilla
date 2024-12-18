package com.vaadin.hilla.crud;

/**
 * A browser-callable service that can list the given type of object.
 *
 * @deprecated Use {@link com.vaadin.flow.spring.data.ListService} instead
 */
@Deprecated(forRemoval = true)
public interface ListService<T>
        extends com.vaadin.flow.spring.data.ListService<T> {

}
