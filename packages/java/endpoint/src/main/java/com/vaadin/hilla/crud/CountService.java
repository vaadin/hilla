package com.vaadin.hilla.crud;

import com.vaadin.hilla.Nullable;
import com.vaadin.hilla.crud.filter.Filter;

/**
 * A browser-callable service that can count the given type of objects with a
 * given filter.
 */
public interface CountService {

    /**
     * Counts the number of entities that match the given filter.
     *
     * @param filter
     *            the filter, or {@code null} to use no filter
     * @return
     */
    public long count(@Nullable Filter filter);

}
