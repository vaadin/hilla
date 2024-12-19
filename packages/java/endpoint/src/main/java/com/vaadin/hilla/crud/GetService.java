package com.vaadin.hilla.crud;

/**
 * A browser-callable service that can fetch the given type of object.
 *
 * @deprecated Use {@link com.vaadin.flow.spring.data.GetService} instead
 */
@Deprecated(forRemoval = true)
public interface GetService<T, ID>
        extends com.vaadin.flow.spring.data.GetService<T, ID> {

}
