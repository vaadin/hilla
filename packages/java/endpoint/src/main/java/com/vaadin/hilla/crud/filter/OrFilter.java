package com.vaadin.hilla.crud.filter;

/**
 * A filter that requires at least one of its children to pass.
 * <p>
 * Custom filter implementations need to handle this filter by running all child
 * filters and verifying that at least one of them passes.
 *
 * @deprecated Use {@link com.vaadin.flow.spring.data.filter.OrFilter} instead
 */
@Deprecated(forRemoval = true)
public class OrFilter extends com.vaadin.flow.spring.data.filter.OrFilter {

}
