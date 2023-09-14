package dev.hilla.crud;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import dev.hilla.crud.filter.PropertyStringFilter;
import org.springframework.data.jpa.domain.Specification;

public class PropertyStringFilterSpecification<T> implements Specification<T> {

    private PropertyStringFilter filter;
    private Class<?> javaType;

    public PropertyStringFilterSpecification(PropertyStringFilter filter,
            Class<?> javaType) {
        this.filter = filter;
        this.javaType = javaType;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder) {
        String value = filter.getFilterValue();
        Path<String> propertyPath = root.get(filter.getPropertyId());
        if (javaType == String.class) {
            Expression<String> expr = criteriaBuilder.lower(propertyPath);
            switch (filter.getMatcher()) {
            case EQUALS:
                return criteriaBuilder.equal(expr, value.toLowerCase());
            case CONTAINS:
                return criteriaBuilder.like(expr,
                        "%" + value.toLowerCase() + "%");
            }

            throw new IllegalArgumentException(
                    "Matcher of type " + filter.getMatcher() + " is unknown");
        } else {
            return criteriaBuilder.equal(propertyPath, value.toLowerCase());
        }
    }

}
