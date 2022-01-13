package com.vaadin.fusion.parser.core;

import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.HierarchicalTypeSignature;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeVariableSignature;

public interface RelativeTypeSignature extends Relative<Relative<?>> {
    static RelativeTypeSignature of(
            @Nonnull HierarchicalTypeSignature signature,
            @Nonnull Relative<?> parent) {
        Objects.requireNonNull(signature);
        Objects.requireNonNull(parent);

        if (signature instanceof BaseTypeSignature) {
            return BaseRelativeTypeSignature.of((BaseTypeSignature) signature,
                    parent);
        } else if (signature instanceof ArrayTypeSignature) {
            return ArrayRelativeTypeSignature.of((ArrayTypeSignature) signature,
                    parent);
        } else if (signature instanceof TypeVariableSignature) {
            return TypeVariableRelativeTypeSignature
                    .of((TypeVariableSignature) signature, parent);
        } else if (signature instanceof TypeArgument) {
            return RelativeTypeArgument.of((TypeArgument) signature, parent);
        } else {
            return ClassRefRelativeTypeSignature
                    .of((ClassRefTypeSignature) signature, parent);
        }
    }

    static RelativeTypeSignature ofNullable(HierarchicalTypeSignature signature,
            Relative<?> parent) {
        return signature == null ? null : of(signature, parent);
    }

    static Stream<ClassInfo> resolve(HierarchicalTypeSignature signature) {
        if (signature == null) {
            return Stream.empty();
        }

        if (signature instanceof BaseTypeSignature) {
            return BaseRelativeTypeSignature
                    .resolve((BaseTypeSignature) signature);
        } else if (signature instanceof ArrayTypeSignature) {
            return ArrayRelativeTypeSignature
                    .resolve((ArrayTypeSignature) signature);
        } else if (signature instanceof TypeVariableSignature) {
            return TypeVariableRelativeTypeSignature
                    .resolve((TypeVariableSignature) signature);
        } else if (signature instanceof TypeArgument) {
            return RelativeTypeArgument.resolve((TypeArgument) signature);
        } else {
            return ClassRefRelativeTypeSignature
                    .resolve((ClassRefTypeSignature) signature);
        }
    }

    @Override
    HierarchicalTypeSignature get();

    @Override
    default Stream<RelativeClassInfo> getDependenciesStream() {
        return resolve(get()).map(RelativeClassInfo::of);
    }

    default boolean hasFloatType() {
        return isFloat() || isDouble();
    }

    default boolean hasIntegerType() {
        return isByte() || isShort() || isInteger() || isLong();
    }

    default boolean isArray() {
        return false;
    }

    default boolean isBase() {
        return false;
    }

    default boolean isBoolean() {
        return false;
    }

    default boolean isByte() {
        return false;
    }

    default boolean isClassRef() {
        return false;
    }

    default boolean isDate() {
        return false;
    }

    default boolean isDateTime() {
        return false;
    }

    default boolean isDouble() {
        return false;
    }

    default boolean isEnum() {
        return false;
    }

    default boolean isFloat() {
        return false;
    }

    default boolean isInteger() {
        return false;
    }

    default boolean isIterable() {
        return false;
    }

    default boolean isLong() {
        return false;
    }

    default boolean isMap() {
        return false;
    }

    default boolean isNativeObject() {
        return false;
    }

    default boolean isOptional() {
        return false;
    }

    default boolean isPrimitive() {
        return false;
    }

    default boolean isShort() {
        return false;
    }

    default boolean isString() {
        return false;
    }

    default boolean isSystem() {
        return false;
    }

    default boolean isTypeArgument() {
        return false;
    }

    default boolean isTypeParameter() {
        return false;
    }

    default boolean isTypeVariable() {
        return false;
    }

    default boolean isVoid() {
        return false;
    }
}
