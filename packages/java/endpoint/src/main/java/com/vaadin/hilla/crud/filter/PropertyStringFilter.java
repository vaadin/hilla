package com.vaadin.hilla.crud.filter;

/**
 * A filter that matches a given property, or nested property path, against a
 * filter value using the specified matcher.
 * <p>
 * Custom filter implementations need to handle this filter by:
 * <ul>
 * <li>Extracting the property value from the object being filtered using
 * {@link #getPropertyId()}.</li>
 * <li>Convert the string representation of the filter value from
 * {@link #getFilterValue()} into a type that can be used for implementing a
 * comparison.</li>
 * <li>Do the actual comparison using the matcher / operator provided by
 * {@link #getMatcher()}</li>
 * </ul>
 *
 * @deprecated Use
 *             {@link com.vaadin.flow.spring.data.filter.PropertyStringFilter}
 *             instead
 */
@Deprecated(forRemoval = true)
public class PropertyStringFilter
        extends com.vaadin.flow.spring.data.filter.PropertyStringFilter {

}
