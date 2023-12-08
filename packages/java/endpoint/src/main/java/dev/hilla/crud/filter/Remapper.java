package dev.hilla.crud.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Remaps property names in filters and pageable objects.
 */
public class Remapper {

    private final Map<String, String> mappings = new HashMap<>();
    private Function<PropertyStringFilter, PropertyStringFilter> filterTransformation;

    /**
     * Declares a mapping from one property name to another. If a filter or
     * pageable is remapped, all occurrences of the property name will be
     * replaced with the new name.
     *
     * @param from
     *            The original property name.
     * @param to
     *            The new property name.
     * @return This instance.
     */
    public Remapper withMapping(String from, String to) {
        mappings.put(from, to);
        return this;
    }

    /**
     * Declares a function that will be applied to all
     * {@link PropertyStringFilter} instances. This can be used to modify the
     * filter value in a more complex way than a simple mapping.
     *
     * @param filterTransformation
     *            The function to apply.
     * @return This instance.
     */
    public Remapper withFilterTransformation(
            Function<PropertyStringFilter, PropertyStringFilter> filterTransformation) {
        this.filterTransformation = filterTransformation;
        return this;
    }

    /**
     * Remaps a filter, replacing all property names with their mapped values.
     *
     * @param filter
     *            The filter to remap.
     * @return The remapped filter.
     */
    public Filter remap(Filter filter) {
        if (filter == null) {
            return null;
        }

        if (filter instanceof AndFilter andFilter) {
            var newAndFilter = new AndFilter();
            newAndFilter.setChildren(
                    andFilter.getChildren().stream().map(this::remap).toList());
            return newAndFilter;
        } else if (filter instanceof OrFilter orFilter) {
            var newOrFilter = new OrFilter();
            newOrFilter.setChildren(
                    orFilter.getChildren().stream().map(this::remap).toList());
            return newOrFilter;
        } else if (filter instanceof PropertyStringFilter propertyStringFilter) {
            var property = propertyStringFilter.getPropertyId();
            var mappedProperty = mappings.get(property);

            var newFilter = new PropertyStringFilter();
            newFilter.setPropertyId(
                    mappedProperty == null ? property : mappedProperty);
            newFilter.setFilterValue(propertyStringFilter.getFilterValue());
            newFilter.setMatcher(propertyStringFilter.getMatcher());

            if (filterTransformation != null) {
                newFilter = filterTransformation.apply(newFilter);
            }

            return newFilter;
        }

        // unknown filters are returned as-is: they are supposed to be already
        // customized according to the use case
        return filter;
    }

    /**
     * Remaps a pageable, replacing all property names with their mapped values.
     *
     * @param pageable
     *            The pageable to remap.
     * @return The remapped pageable.
     */
    public Pageable remap(Pageable pageable) {
        if (pageable == null) {
            return null;
        }

        var orders = pageable.getSort().stream().map(order -> {
            var mappedProperty = mappings.get(order.getProperty());
            return mappedProperty == null ? order
                    : new Sort.Order(order.getDirection(), mappedProperty);
        }).toList();

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(orders));
    }
}
