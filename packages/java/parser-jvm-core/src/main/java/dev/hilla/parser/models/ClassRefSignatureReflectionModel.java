package dev.hilla.parser.models;

import java.lang.reflect.ParameterizedType;
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
import java.util.Set;
import java.util.stream.Collectors;

final class ClassRefSignatureReflectionModel
        extends AbstractReflectionSignatureModel<Class<?>>
        implements ClassRefSignatureModel, ReflectionModel {
    private static final Class<?>[] DATE_CLASSES = { Date.class,
            LocalDate.class };
    private static final Class<?>[] DATE_TIME_CLASSES = { LocalDateTime.class,
            Instant.class, LocalTime.class };

    private Collection<TypeArgumentModel> typeArguments;
    private ParameterizedType wrapper;

    public ClassRefSignatureReflectionModel(Class<?> origin) {
        this(origin, null, null);
    }

    public ClassRefSignatureReflectionModel(Class<?> origin, Model parent) {
        this(origin, null, parent);
    }

    public ClassRefSignatureReflectionModel(Class<?> origin,
            ParameterizedType wrapper, Model parent) {
        super(origin, parent);
        this.wrapper = wrapper;
    }

    @Override
    public Collection<TypeArgumentModel> getTypeArguments() {
        if (typeArguments == null) {
            typeArguments = wrapper != null
                    ? Arrays.stream(wrapper.getActualTypeArguments())
                            .map(arg -> TypeArgumentModel.of(arg, this))
                            .collect(Collectors.toSet())
                    : Set.of();
        }

        return typeArguments;
    }

    @Override
    public boolean isBoolean() {
        return Boolean.class.isAssignableFrom(origin);
    }

    @Override
    public boolean isByte() {
        return Byte.class.isAssignableFrom(origin);
    }

    @Override
    public boolean isCharacter() {
        return Character.class.isAssignableFrom(origin);
    }

    @Override
    public boolean isDate() {
        return Arrays.stream(DATE_CLASSES)
                .anyMatch(cls -> cls.isAssignableFrom(origin));
    }

    @Override
    public boolean isDateTime() {
        return Arrays.stream(DATE_TIME_CLASSES)
                .anyMatch(cls -> cls.isAssignableFrom(origin));
    }

    @Override
    public boolean isDouble() {
        return Double.class.isAssignableFrom(origin);
    }

    @Override
    public boolean isEnum() {
        return origin.isEnum();
    }

    @Override
    public boolean isFloat() {
        return Float.class.isAssignableFrom(origin);
    }

    @Override
    public boolean isInteger() {
        return Integer.class.isAssignableFrom(origin);
    }

    @Override
    public boolean isIterable() {
        return Iterable.class.isAssignableFrom(origin);
    }

    @Override
    public boolean isJDKClass() {
        return ModelUtils.isJDKClass(origin.getName());
    }

    @Override
    public boolean isLong() {
        return Long.class.isAssignableFrom(origin);
    }

    @Override
    public boolean isMap() {
        return Map.class.isAssignableFrom(origin);
    }

    @Override
    public boolean isNativeObject() {
        return Objects.equals(origin, Object.class);
    }

    @Override
    public boolean isOptional() {
        return Optional.class.isAssignableFrom(origin);
    }

    @Override
    public boolean isShort() {
        return Short.class.isAssignableFrom(origin);
    }

    @Override
    public boolean isString() {
        return String.class.isAssignableFrom(origin);
    }
}
