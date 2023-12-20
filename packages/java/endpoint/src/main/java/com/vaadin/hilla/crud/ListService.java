package com.vaadin.hilla.crud;

import java.util.List;

import com.vaadin.hilla.Nonnull;
import com.vaadin.hilla.Nullable;
import com.vaadin.hilla.crud.filter.Filter;
import org.springframework.data.domain.Pageable;

/**
 * A browser-callable service that can list the given type of object.
 */
public interface ListService<T> {
    /**
     * Lists objects of the given type using the paging, sorting and filtering
     * options provided in the parameters.
     *
     * @param pageable
     *            contains information about paging and sorting
     * @param filter
     *            the filter to apply or {@code null} to not filter
     * @return a list of objects or an empty list if no objects were found
     */
    @Nonnull
    List<@Nonnull T> list(Pageable pageable, @Nullable Filter filter);

}
