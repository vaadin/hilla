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
            case GREATER_THAN:
                throw new IllegalArgumentException(
                        "A string cannot be filtered using greater than");
            case LESS_THAN:
                throw new IllegalArgumentException(
                        "A string cannot be filtered using less than");
            default:
                break;
            }

        } else if (isNumber(javaType)) {
            switch (filter.getMatcher()) {
            case EQUALS:
                return criteriaBuilder.equal(propertyPath, value);
            case CONTAINS:
                throw new IllegalArgumentException(
                        "A number cannot be filtered using contains");
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(propertyPath, value);
            case LESS_THAN:
                return criteriaBuilder.lessThan(propertyPath, value);
            default:
                break;
            }
        } else if (isBoolean(javaType)) {
            Boolean booleanValue = Boolean.valueOf(value);
            switch (filter.getMatcher()) {
            case EQUALS:
                return criteriaBuilder.equal(propertyPath, booleanValue);
            case CONTAINS:
                throw new IllegalArgumentException(
                        "A boolean cannot be filtered using contains");
            case GREATER_THAN:
                throw new IllegalArgumentException(
                        "A boolean cannot be filtered using greater than");
            case LESS_THAN:
                throw new IllegalArgumentException(
                        "A boolean cannot be filtered using less than");
            default:
                break;
            }
        }
        throw new IllegalArgumentException("No implementation for " + javaType
                + " using " + filter.getMatcher() + ".");
    }

    private boolean isNumber(Class<?> javaType) {
        return javaType == int.class || javaType == Integer.class
                || javaType == double.class || javaType == Double.class
                || javaType == long.class || javaType == Long.class;
    }

    private boolean isBoolean(Class<?> javaType) {
        return javaType == boolean.class || javaType == Boolean.class;
    }

}
