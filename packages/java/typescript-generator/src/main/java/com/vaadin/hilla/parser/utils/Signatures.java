/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

/**
 * Tells what a type signature the parser walks stands for.
 *
 * <p>
 * A type signature is the reflection object a typed node of the walk holds: an
 * {@link AnnotatedType} where a type is used, such as the type of a field or a
 * type argument of it, and a {@link TypeVariable} where a type parameter is
 * declared. An annotated type is one of these kinds:
 *
 * <ul>
 * <li>a class reference: a class or an interface, with or without type
 * arguments ({@link #isClassRef(AnnotatedElement)})</li>
 * <li>a primitive type, {@code void} included
 * ({@link #isBase(AnnotatedElement)})</li>
 * <li>an array ({@link AnnotatedArrayType})</li>
 * <li>a type variable ({@link AnnotatedTypeVariable})</li>
 * <li>a wildcard type argument ({@link AnnotatedWildcardType})</li>
 * </ul>
 */
public final class Signatures {
    private static final List<Class<?>> DATE_CLASSES = List.of(Date.class,
            LocalDate.class);
    private static final List<Class<?>> DATE_TIME_CLASSES = List.of(
            LocalDateTime.class, Instant.class, LocalTime.class,
            OffsetDateTime.class, ZonedDateTime.class);

    private Signatures() {
    }

    /**
     * Creates the signature of a class used without annotations or type
     * arguments, for a type reflection gives as a class only.
     *
     * @param cls
     *            the class
     * @return the signature of the class
     */
    public static AnnotatedType of(@NonNull Class<?> cls) {
        return new BareType(Objects.requireNonNull(cls));
    }

    /**
     * Creates the signature of a class reference which refers to another class
     * than the given one, keeping its type arguments and annotations.
     *
     * @param type
     *            the class reference to take the type arguments and annotations
     *            from
     * @param cls
     *            the class to refer to instead
     * @return the signature referring to the class
     */
    public static AnnotatedType withClass(@NonNull AnnotatedType type,
            @NonNull Class<?> cls) {
        var typeArguments = getTypeArguments(type);
        var annotations = List.of(type.getAnnotations());

        return typeArguments.isEmpty() ? new ReplacedType(cls, annotations)
                : new ReplacedParameterizedType(cls, typeArguments,
                        annotations);
    }

    /**
     * The annotations the signature is used with, or, for a type parameter, the
     * annotations it is declared with.
     *
     * @param signature
     *            the signature
     * @return the annotations
     */
    public static List<Annotation> getAnnotations(
            @NonNull AnnotatedElement signature) {
        return List.of(signature.getAnnotations());
    }

    /**
     * The class a class reference or a primitive type refers to.
     *
     * @param signature
     *            the signature
     * @return the class, or empty for any other kind of signature
     */
    public static Optional<Class<?>> getClass(
            @NonNull AnnotatedElement signature) {
        if (!(signature instanceof AnnotatedType annotated)
                || isNotPlain(annotated)) {
            return Optional.empty();
        }

        return switch (annotated.getType()) {
        case Class<?> cls -> Optional.of(cls);
        case ParameterizedType parameterized ->
            Optional.of((Class<?>) parameterized.getRawType());
        default -> Optional.empty();
        };
    }

    /**
     * The type arguments of a class reference.
     *
     * @param signature
     *            the signature
     * @return the type arguments, or an empty list for a signature which has
     *         none
     */
    public static List<AnnotatedType> getTypeArguments(
            @NonNull AnnotatedElement signature) {
        return signature instanceof AnnotatedParameterizedType parameterized
                ? List.of(parameterized.getAnnotatedActualTypeArguments())
                : List.of();
    }

    /**
     * The types a wildcard type argument is bound by, except {@link Object},
     * which bounds a wildcard that says nothing.
     *
     * @param wildcard
     *            the wildcard
     * @return the lower bounds followed by the upper bounds
     */
    public static List<AnnotatedType> getBounds(
            @NonNull AnnotatedWildcardType wildcard) {
        return Streams
                .combine(wildcard.getAnnotatedLowerBounds(),
                        wildcard.getAnnotatedUpperBounds())
                .filter(bound -> bound.getType() != Object.class).distinct()
                .toList();
    }

    /**
     * The types a type parameter is bound by.
     *
     * @param typeParameter
     *            the type parameter
     * @return the bounds
     */
    public static List<AnnotatedType> getBounds(
            @NonNull TypeVariable<?> typeParameter) {
        return List.of(typeParameter.getAnnotatedBounds());
    }

    /**
     * The type parameter a type variable stands for.
     *
     * @param typeVariable
     *            the type variable
     * @return the type parameter declaring it
     */
    public static TypeVariable<?> resolve(
            @NonNull AnnotatedTypeVariable typeVariable) {
        return (TypeVariable<?>) typeVariable.getType();
    }

    public static boolean isArray(AnnotatedElement signature) {
        return signature instanceof AnnotatedArrayType;
    }

    public static boolean isBase(AnnotatedElement signature) {
        return getClass(signature).filter(Class::isPrimitive).isPresent();
    }

    public static boolean isClassRef(AnnotatedElement signature) {
        return getClass(signature).filter(cls -> !cls.isPrimitive())
                .isPresent();
    }

    public static boolean isTypeArgument(AnnotatedElement signature) {
        return signature instanceof AnnotatedWildcardType;
    }

    public static boolean isTypeParameter(AnnotatedElement signature) {
        return signature instanceof TypeVariable<?>;
    }

    public static boolean isTypeVariable(AnnotatedElement signature) {
        return signature instanceof AnnotatedTypeVariable;
    }

    public static boolean isBigDecimal(AnnotatedElement signature) {
        return isClassRefOf(signature, BigDecimal.class);
    }

    public static boolean isBigInteger(AnnotatedElement signature) {
        return isClassRefOf(signature, BigInteger.class);
    }

    public static boolean isBoolean(AnnotatedElement signature) {
        return is(signature, Boolean.TYPE, Boolean.class);
    }

    public static boolean isByte(AnnotatedElement signature) {
        return is(signature, Byte.TYPE, Byte.class);
    }

    public static boolean isCharacter(AnnotatedElement signature) {
        return is(signature, Character.TYPE, Character.class);
    }

    public static boolean isDate(AnnotatedElement signature) {
        return DATE_CLASSES.stream()
                .anyMatch(cls -> isClassRefOf(signature, cls));
    }

    public static boolean isDateTime(AnnotatedElement signature) {
        return DATE_TIME_CLASSES.stream()
                .anyMatch(cls -> isClassRefOf(signature, cls));
    }

    public static boolean isDouble(AnnotatedElement signature) {
        return is(signature, Double.TYPE, Double.class);
    }

    public static boolean isEnum(AnnotatedElement signature) {
        return getClass(signature).filter(Class::isEnum).isPresent();
    }

    public static boolean isFloat(AnnotatedElement signature) {
        return is(signature, Float.TYPE, Float.class);
    }

    public static boolean isInteger(AnnotatedElement signature) {
        return is(signature, Integer.TYPE, Integer.class);
    }

    public static boolean isIterable(AnnotatedElement signature) {
        return isClassRefOf(signature, Iterable.class);
    }

    public static boolean isLong(AnnotatedElement signature) {
        return is(signature, Long.TYPE, Long.class);
    }

    public static boolean isMap(AnnotatedElement signature) {
        return isClassRefOf(signature, Map.class);
    }

    public static boolean isNativeObject(AnnotatedElement signature) {
        return getClass(signature).filter(cls -> cls == Object.class)
                .isPresent();
    }

    public static boolean isOptional(AnnotatedElement signature) {
        return isClassRefOf(signature, Optional.class);
    }

    public static boolean isShort(AnnotatedElement signature) {
        return is(signature, Short.TYPE, Short.class);
    }

    public static boolean isString(AnnotatedElement signature) {
        return isClassRefOf(signature, String.class);
    }

    public static boolean isPrimitive(AnnotatedElement signature) {
        return isBase(signature) && !isVoid(signature);
    }

    public static boolean isVoid(AnnotatedElement signature) {
        return getClass(signature).filter(cls -> cls == Void.TYPE).isPresent();
    }

    public static boolean hasFloatType(AnnotatedElement signature) {
        return isFloat(signature) || isDouble(signature);
    }

    public static boolean hasIntegerType(AnnotatedElement signature) {
        return isByte(signature) || isShort(signature) || isInteger(signature)
                || isLong(signature);
    }

    /**
     * Whether a signature refers to a class of the JDK. A primitive type counts
     * as one; an array, a type variable, a type parameter and a wildcard do
     * not.
     *
     * @param signature
     *            the signature
     * @return {@code true} if the signature refers to a JDK class
     */
    public static boolean isJDKClass(AnnotatedElement signature) {
        return getClass(signature).filter(Signatures::isJDKClass).isPresent();
    }

    public static boolean isNonJDKClass(AnnotatedElement signature) {
        return !isJDKClass(signature);
    }

    public static boolean isJDKClass(Class<?> cls) {
        return cls.isPrimitive() || isJDKClass(cls.getName());
    }

    public static boolean isJDKClass(String name) {
        return name.startsWith("java") || name.startsWith("com.sun")
                || name.startsWith("sun") || name.startsWith("oracle")
                || name.startsWith("org.xml") || name.startsWith("com.oracle");
    }

    /**
     * Whether a class is the named class or extends it. Only the superclasses
     * are checked, not the interfaces.
     *
     * @param target
     *            the name of the class to look for
     * @param actor
     *            the class to check
     * @return {@code true} if the class is or extends the named class
     */
    public static boolean isAssignableFrom(String target, Class<?> actor) {
        while (actor != null) {
            if (Objects.equals(target, actor.getName())) {
                return true;
            }

            actor = actor.getSuperclass();
        }

        return false;
    }

    private static boolean is(AnnotatedElement signature, Class<?> primitive,
            Class<?> wrapper) {
        return getClass(signature)
                .filter(cls -> cls.isPrimitive() ? cls == primitive
                        : wrapper.isAssignableFrom(cls))
                .isPresent();
    }

    private static boolean isClassRefOf(AnnotatedElement signature,
            Class<?> target) {
        return getClass(signature).filter(
                cls -> !cls.isPrimitive() && target.isAssignableFrom(cls))
                .isPresent();
    }

    private static boolean isNotPlain(AnnotatedType type) {
        return type instanceof AnnotatedArrayType
                || type instanceof AnnotatedTypeVariable
                || type instanceof AnnotatedWildcardType;
    }

    private interface SyntheticType extends AnnotatedType {
        List<Annotation> annotations();

        @Override
        default <T extends Annotation> T getAnnotation(
                Class<T> annotationClass) {
            return annotations().stream().filter(annotationClass::isInstance)
                    .map(annotationClass::cast).findFirst().orElse(null);
        }

        @Override
        default Annotation[] getAnnotations() {
            return annotations().toArray(Annotation[]::new);
        }

        @Override
        default Annotation[] getDeclaredAnnotations() {
            return getAnnotations();
        }

        @Override
        default AnnotatedType getAnnotatedOwnerType() {
            return null;
        }
    }

    private record BareType(Class<?> type) implements SyntheticType {
        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public List<Annotation> annotations() {
            return List.of();
        }

        @Override
        public String toString() {
            return type.getTypeName();
        }
    }

    private record ReplacedType(Class<?> type,
            List<Annotation> annotations) implements SyntheticType {
        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public String toString() {
            return Stream
                    .concat(annotations.stream().map(Annotation::toString),
                            Stream.of(type.getTypeName()))
                    .collect(Collectors.joining(" "));
        }
    }

    private record ReplacedParameterizedType(Class<?> rawType,
            List<AnnotatedType> typeArguments, List<Annotation> annotations)
            implements
                SyntheticType,
                AnnotatedParameterizedType {
        @Override
        public AnnotatedType getAnnotatedOwnerType() {
            return null;
        }

        @Override
        public AnnotatedType[] getAnnotatedActualTypeArguments() {
            return typeArguments.toArray(AnnotatedType[]::new);
        }

        @Override
        public Type getType() {
            return new Parameterized(rawType, typeArguments.stream()
                    .map(AnnotatedType::getType).toList());
        }

        @Override
        public String toString() {
            return getType().getTypeName();
        }
    }

    private record Parameterized(Class<?> rawType,
            List<Type> typeArguments) implements ParameterizedType {
        @Override
        public Class<?> getRawType() {
            return rawType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.toArray(Type[]::new);
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        @Override
        public String getTypeName() {
            return rawType.getTypeName()
                    + typeArguments.stream().map(Type::getTypeName)
                            .collect(Collectors.joining(", ", "<", ">"));
        }

        @Override
        public String toString() {
            return getTypeName();
        }
    }
}
