package com.vaadin.fusion.parser.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.TypeSignature;

public final class ClassRefEnhancedTypeSignature extends EnhancedTypeSignature {
    private static final Class<?>[] DATE_CLASSES = { Date.class,
            LocalDate.class };
    private static final Class<?>[] DATE_TIME_CLASSES = { LocalDateTime.class,
            Instant.class, LocalTime.class };
    private static final Class<?>[] NUMBER_CLASSES = { Byte.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class };

    private final Class<?> currentClass;

    public ClassRefEnhancedTypeSignature(TypeSignature signature) {
        super(signature);
        currentClass = ((ClassRefTypeSignature) signature).loadClass();
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isBase() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return Objects.equals(((ClassRefTypeSignature) signature)
                .getFullyQualifiedClassName(), Boolean.class.getName());
    }

    @Override
    public boolean isClassRef() {
        return true;
    }

    @Override
    public boolean isCollection() {
        return Collection.class.isAssignableFrom(currentClass);
    }

    @Override
    public boolean isDate() {
        return Arrays.stream(DATE_CLASSES)
                .anyMatch(cls -> cls.isAssignableFrom(currentClass));
    }

    @Override
    public boolean isDateTime() {
        return Arrays.stream(DATE_TIME_CLASSES)
                .anyMatch(cls -> cls.isAssignableFrom(currentClass));
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public boolean isMap() {
        return Map.class.isAssignableFrom(currentClass);
    }

    @Override
    public boolean isNumber() {
        return Arrays.stream(NUMBER_CLASSES)
                .anyMatch(cls -> cls.isAssignableFrom(currentClass));
    }

    @Override
    public boolean isOptional() {
        return Optional.class.isAssignableFrom(currentClass);
    }

    @Override
    public boolean isString() {
        return String.class.isAssignableFrom(currentClass);
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return ((ClassRefTypeSignature) signature).getClassInfo() == null;
    }

    @Override
    public boolean isVoid() {
        return false;
    }
}
