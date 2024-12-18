package com.vaadin.hilla.crud.filter;

/**
 * Superclass for all filters to be used with CRUD services. This specific class
 * is never used, instead a filter instance will be one of the following types:
 * <ul>
 * <li>{@link AndFilter} - Contains a list of nested filters, all of which need
 * to pass.</li>
 * <li>{@link OrFilter} - Contains a list of nested filters, of which at least
 * one needs to pass.</li>
 * <li>{@link PropertyStringFilter} - Matches a specific property, or nested
 * property path, against a filter value, using a specific operator.</li>
 * </ul>
 *
 * @deprecated Use {@link com.vaadin.flow.spring.data.filter.Filter} instead
 */
@Deprecated(forRemoval = true)
public class Filter extends com.vaadin.flow.spring.data.filter.Filter {

}
