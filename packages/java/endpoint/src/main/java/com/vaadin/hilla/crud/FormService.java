package com.vaadin.hilla.crud;

/**
 * A browser-callable service that can create, update, and delete a given type
 * of object.
 *
 * @deprecated Use {@link com.vaadin.flow.spring.data.FormService} instead
 */
@Deprecated(forRemoval = true)
public interface FormService<T, ID>
        extends com.vaadin.flow.spring.data.FormService<T, ID> {

}
