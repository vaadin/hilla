package dev.hilla.parser.models;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ClassRefSignatureReflectionModel extends
        AbstractReflectionSignatureDependable<Type, Dependable<?, ?>>
        implements ClassRefSignatureModel, ReflectionSignatureModel {
    private static final Class<?>[] DATE_CLASSES = { Date.class,
            LocalDate.class };
    private static final Class<?>[] DATE_TIME_CLASSES = { LocalDateTime.class,
            Instant.class, LocalTime.class };

    private Collection<TypeArgumentModel> typeArguments;

    public ClassRefSignatureReflectionModel(Type origin) {
        super(origin, null);
    }

    public ClassRefSignatureReflectionModel(Type origin,
            Dependable<?, ?> parent) {
        super(origin, parent);
    }

    @Override
    public Collection<TypeArgumentModel> getTypeArguments() {
        if (typeArguments == null) {
            if (origin instanceof ParameterizedType) {
                typeArguments = Stream
                    .of(origin.getTypeArguments().stream(),
                        origin.getSuffixTypeArguments().stream()
                            .flatMap(Collection::stream))
                    .flatMap(Function.identity())
                    .map(arg -> TypeArgumentModel.of(arg, this))
                    .collect(Collectors.toList());
            } else if (origin instanceof Class<?>) {
                typeArguments = Set.of();
            }
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
