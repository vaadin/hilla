package dev.hilla.crud;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import dev.hilla.crud.filter.PropertyStringFilter;
import org.springframework.data.jpa.domain.Specification;

public class PropertyStringFilterSpecification<T> implements Specification<T> {

    private PropertyStringFilter filter;
    private Class<T> entity;
    private Class<?> javaType;

    public PropertyStringFilterSpecification(PropertyStringFilter filter,
            Class<T> entity, Class<?> javaType) {
        this.filter = filter;
        this.entity = entity;
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
        } else if (javaType == LocalDate.class) {
            TemporalAccessor date = DateTimeFormatter.ISO_DATE.parse(value);
            return criteriaBuilder.equal(propertyPath, LocalDate.from(date));
        } else if (javaType == LocalTime.class) {
            TemporalAccessor date = DateTimeFormatter.ISO_TIME.parse(value);
            return criteriaBuilder.equal(propertyPath, LocalDate.from(date));
        } else if (javaType == LocalDateTime.class) {
            TemporalAccessor date = DateTimeFormatter.ISO_DATE_TIME
                    .parse(value);
            return criteriaBuilder.equal(propertyPath, LocalDate.from(date));
        } else {
            return criteriaBuilder.equal(propertyPath, value.toLowerCase());
        }
    }

}
