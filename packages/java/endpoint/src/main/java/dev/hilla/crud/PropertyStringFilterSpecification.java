package dev.hilla.crud;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import dev.hilla.crud.filter.PropertyStringFilter;
import org.springframework.data.jpa.domain.Specification;

public class PropertyStringFilterSpecification<T> implements Specification<T> {

    private final PropertyStringFilter filter;
    private final Class<?> javaType;

    public PropertyStringFilterSpecification(PropertyStringFilter filter,
            Class<?> javaType) {
        this.filter = filter;
        this.javaType = javaType;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder) {
        String value = filter.getFilterValue();
        Path<String> propertyPath = getPath(filter.getPropertyId(), root);
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
        } else if (isDate(javaType)) {
            var path = root.<Date> get(filter.getPropertyId());
            var dateValue = Date.from(LocalDate.parse(value)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
            switch (filter.getMatcher()) {
            case EQUALS:
                return criteriaBuilder.equal(path, dateValue);
            case CONTAINS:
                throw new IllegalArgumentException(
                        "A date cannot be filtered using contains");
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(path, dateValue);
            case LESS_THAN:
                return criteriaBuilder.lessThan(path, dateValue);
            default:
                break;
            }
        } else if (isLocalDate(javaType)) {
            var path = root.<LocalDate> get(filter.getPropertyId());
            var dateValue = LocalDate.parse(value);
            switch (filter.getMatcher()) {
            case EQUALS:
                return criteriaBuilder.equal(path, dateValue);
            case CONTAINS:
                throw new IllegalArgumentException(
                        "A date cannot be filtered using contains");
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(path, dateValue);
            case LESS_THAN:
                return criteriaBuilder.lessThan(path, dateValue);
            default:
                break;
            }
        } else if (isLocalTime(javaType)) {
            var path = root.<LocalTime> get(filter.getPropertyId());
            var timeValue = LocalTime.parse(value);
            switch (filter.getMatcher()) {
            case EQUALS:
                return criteriaBuilder.equal(path, timeValue);
            case CONTAINS:
                throw new IllegalArgumentException(
                        "A time cannot be filtered using contains");
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(path, timeValue);
            case LESS_THAN:
                return criteriaBuilder.lessThan(path, timeValue);
            default:
                break;
            }
        } else if (isLocalDateTime(javaType)) {
            var path = root.<LocalDateTime> get(filter.getPropertyId());
            var dateValue = LocalDate.parse(value);
            var minValue = LocalDateTime.of(dateValue, LocalTime.MIN);
            var maxValue = LocalDateTime.of(dateValue, LocalTime.MAX);
            switch (filter.getMatcher()) {
            case EQUALS:
                return criteriaBuilder.between(path, minValue, maxValue);
            case CONTAINS:
                throw new IllegalArgumentException(
                        "A datetime cannot be filtered using contains");
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(path, maxValue);
            case LESS_THAN:
                return criteriaBuilder.lessThan(path, minValue);
            default:
                break;
            }
        } else if (javaType.isEnum()) {
            var enumValue = Enum.valueOf(javaType.asSubclass(Enum.class),
                    value);

            switch (filter.getMatcher()) {
            case EQUALS:
                return criteriaBuilder.equal(propertyPath, enumValue);
            case CONTAINS:
                throw new IllegalArgumentException(
                        "An enum cannot be filtered using contains");
            case GREATER_THAN:
                throw new IllegalArgumentException(
                        "An enum cannot be filtered using greater than");
            case LESS_THAN:
                throw new IllegalArgumentException(
                        "An enum cannot be filtered using less than");
            default:
                break;
            }
        }
        throw new IllegalArgumentException("No implementation for " + javaType
                + " using " + filter.getMatcher() + ".");
    }

    private boolean isNumber(Class<?> javaType) {
        return javaType == int.class || javaType == Integer.class
                || javaType == long.class || javaType == Long.class
                || javaType == float.class || javaType == Float.class
                || javaType == double.class || javaType == Double.class;
    }

    private Path<String> getPath(String propertyId, Root<T> root) {
        String[] parts = propertyId.split("\\.");
        Path<String> path = root.get(parts[0]);
        int i = 1;
        while (i < parts.length) {
            path = path.get(parts[i]);
            i++;
        }
        return path;
    }

    private boolean isBoolean(Class<?> javaType) {
        return javaType == boolean.class || javaType == Boolean.class;
    }

    private boolean isDate(Class<?> javaType) {
        return javaType == java.util.Date.class;
    }

    private boolean isLocalDate(Class<?> javaType) {
        return javaType == java.time.LocalDate.class;
    }

    private boolean isLocalTime(Class<?> javaType) {
        return javaType == java.time.LocalTime.class;
    }

    private boolean isLocalDateTime(Class<?> javaType) {
        return javaType == java.time.LocalDateTime.class;
    }
}
