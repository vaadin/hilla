package dev.hilla.crud;

import jakarta.persistence.EntityManager;

import dev.hilla.crud.filter.AndFilter;
import dev.hilla.crud.filter.Filter;
import dev.hilla.crud.filter.OrFilter;
import dev.hilla.crud.filter.PropertyStringFilter;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Utility class for converting Hilla {@link Filter} specifications into JPA
 * filter specifications. This class can be used to implement filtering for
 * custom {@link ListService} or {@link CrudService} implementations that use
 * JPA as the data source.
 * <p>
 * This class requires an EntityManager to be available as a Spring bean and
 * thus should be injected into the bean that wants to use the converter.
 * Manually creating new instances of this class will not work.
 */
@Component
public class JpaFilterConverter {

    @Autowired
    private EntityManager em;

    /**
     * Converts the given Hilla filter specification into a JPA filter
     * specification for the specified entity class.
     * <p>
     * If the filter contains {@link PropertyStringFilter} instances, their
     * properties, or nested property paths, need to match the structure of the
     * entity class. Likewise, their filter values should be in a format that
     * can be parsed into the type that the property is of.
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
        if (rawFilter == null) {
            return Specification.anyOf();
        }
        if (rawFilter instanceof AndFilter filter) {
            return Specification.allOf(filter.getChildren().stream()
                    .map(f -> toSpec(f, entity)).toList());
        } else if (rawFilter instanceof OrFilter filter) {
            return Specification.anyOf(filter.getChildren().stream()
                    .map(f -> toSpec(f, entity)).toList());
        } else if (rawFilter instanceof PropertyStringFilter filter) {
            Class<?> javaType = extractPropertyJavaType(entity,
                    filter.getPropertyId());
            return new PropertyStringFilterSpecification<>(filter, javaType);
        } else {
            if (rawFilter != null) {
                throw new IllegalArgumentException("Unknown filter type "
                        + rawFilter.getClass().getName());
            }
            return Specification.anyOf();
        }
    }

    private Class<?> extractPropertyJavaType(Class<?> entity,
            String propertyId) {
        if (propertyId.contains(".")) {
            String[] parts = propertyId.split("\\.");
            Root<?> root = em.getCriteriaBuilder().createQuery(entity)
                    .from(entity);
            Path<?> path = root.get(parts[0]);
            int i = 1;
            while (i < parts.length) {
                path = path.get(parts[i]);
                i++;
            }
            return path.getJavaType();
        } else {
            return em.getMetamodel().entity(entity).getAttribute(propertyId)
                    .getJavaType();
        }
    }

}
