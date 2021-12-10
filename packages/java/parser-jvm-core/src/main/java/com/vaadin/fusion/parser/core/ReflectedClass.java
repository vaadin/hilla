package com.vaadin.fusion.parser.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;

public final class ReflectedClass {
    private static final Class<?>[] DATE_CLASSES = { Date.class,
            LocalDate.class };
    private static final Class<?>[] DATE_TIME_CLASSES = { LocalDateTime.class,
            Instant.class, LocalTime.class };

    private final Class<?> origin;

    public ReflectedClass(ClassInfo info) {
        this(info.loadClass());
    }

    public ReflectedClass(RelativeClassInfo info) {
        this(info.get());
    }

    public ReflectedClass(ClassRefTypeSignature signature) {
        this(signature.loadClass());
    }

    public ReflectedClass(ClassRefRelativeTypeSignature signature) {
        this(signature.get());
    }

    public ReflectedClass(Class<?> origin) {
        this.origin = origin;
    }

    public boolean hasFloatType() {
        return isFloat() || isDouble();
    }

    public boolean hasIntegerType() {
        return isByte() || isShort() || isInteger() || isLong();
    }

    public boolean isBoolean() {
        return Boolean.class.isAssignableFrom(origin);
    }

    public boolean isByte() {
        return Byte.class.isAssignableFrom(origin);
    }

    public boolean isCharacter() {
        return Character.class.isAssignableFrom(origin);
    }

    public boolean isDate() {
        return Arrays.stream(DATE_CLASSES)
                .anyMatch(cls -> cls.isAssignableFrom(origin));
    }

    public boolean isDateTime() {
        return Arrays.stream(DATE_TIME_CLASSES)
                .anyMatch(cls -> cls.isAssignableFrom(origin));
    }

    public boolean isDouble() {
        return Double.class.isAssignableFrom(origin);
    }

    public boolean isEnum() {
        return origin.isEnum();
    }

    public boolean isFloat() {
        return Float.class.isAssignableFrom(origin);
    }

    public boolean isInteger() {
        return Integer.class.isAssignableFrom(origin);
    }

    public boolean isIterable() {
        return Iterable.class.isAssignableFrom(origin);
    }

    public boolean isLong() {
        return Long.class.isAssignableFrom(origin);
    }

    public boolean isMap() {
        return Map.class.isAssignableFrom(origin);
    }

    public boolean isOptional() {
        return Optional.class.isAssignableFrom(origin);
    }

    public boolean isShort() {
        return Short.class.isAssignableFrom(origin);
    }

    public boolean isString() {
        return String.class.isAssignableFrom(origin);
    }
}
