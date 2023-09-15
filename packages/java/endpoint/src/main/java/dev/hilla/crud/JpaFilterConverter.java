package dev.hilla.crud;

import jakarta.persistence.EntityManager;

import dev.hilla.crud.filter.AndFilter;
import dev.hilla.crud.filter.Filter;
import dev.hilla.crud.filter.OrFilter;
import dev.hilla.crud.filter.PropertyStringFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class JpaFilterConverter {

    @Autowired
    private EntityManager em;

    /**
     * Converts the given Hilla filter specification into a JPA filter
     * specification.
     * 
     * @param <T>
     *            the type of the entity
     * @param rawFilter
     *            the filter to convert
     * @param entity
     *            the entity class
     * @return a JPA filter specification for the given filter
     */
    public <T> Specification<T> toSpec(Filter rawFilter, Class<T> entity) {
        if (rawFilter instanceof AndFilter filter) {
            return Specification.allOf(filter.getChildren().stream()
                    .map(f -> toSpec(f, entity)).toList());
        } else if (rawFilter instanceof OrFilter filter) {
            return Specification.anyOf(filter.getChildren().stream()
                    .map(f -> toSpec(f, entity)).toList());
        } else if (rawFilter instanceof PropertyStringFilter filter) {
            Class<?> javaType = em.getMetamodel().entity(entity)
                    .getAttribute(filter.getPropertyId()).getJavaType();

            return new PropertyStringFilterSpecification<>(filter, javaType);

        } else {
            if (rawFilter != null) {
                throw new IllegalArgumentException("Unknown filter type "
                        + rawFilter.getClass().getName());
            }
            return Specification.anyOf();
        }
    }

}
