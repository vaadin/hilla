package dev.hilla.parser.models;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.HierarchicalTypeSignature;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeVariableSignature;

public interface SignatureModel extends Model, Dependable {
    static SignatureModel of(@Nonnull HierarchicalTypeSignature signature,
                             Model parent) {
        if (signature instanceof BaseTypeSignature) {
            return BaseSignatureModel.of((BaseTypeSignature) signature, parent);
        } else if (signature instanceof ArrayTypeSignature) {
            return ArraySignatureModel.of((ArrayTypeSignature) signature,
                    parent);
        } else if (signature instanceof TypeVariableSignature) {
            return TypeVariableModel.of((TypeVariableSignature) signature,
                    parent);
        } else if (signature instanceof io.github.classgraph.TypeArgument) {
            return TypeArgumentModel
                    .of((io.github.classgraph.TypeArgument) signature, parent);
        } else {
            return ClassRefSignatureModel.of((ClassRefTypeSignature) signature,
                    parent);
        }
    }

    static SignatureModel of(@Nonnull Type signature, Model parent) {
        if (signature instanceof ParameterizedType) {
            return ClassRefSignatureModel.of((ParameterizedType) signature,
                    parent);
        } else if (signature instanceof TypeVariable<?>) {
            return TypeVariableModel.of((TypeVariable<?>) signature, parent);
        } else if (signature instanceof WildcardType) {
            return TypeArgumentModel.of(signature, parent);
        } else if (signature instanceof GenericArrayType) {
            return ArraySignatureModel.of(signature, parent);
        } else if (((Class<?>) signature).isPrimitive()) {
            return BaseSignatureModel.of((Class<?>) signature, parent);
        } else if (((Class<?>) signature).isArray()) {
            return ArraySignatureModel.of(signature, parent);
        } else {
            return ClassRefSignatureModel.of((Class<?>) signature, parent);
        }
    }

    static Stream<ClassInfo> resolveDependencies(
            HierarchicalTypeSignature signature) {
        if (signature == null) {
            return Stream.empty();
        }

        if (signature instanceof BaseTypeSignature) {
            return BaseSignatureModel
                    .resolveDependencies((BaseTypeSignature) signature);
        } else if (signature instanceof ArrayTypeSignature) {
            return ArraySignatureModel
                    .resolveDependencies((ArrayTypeSignature) signature);
        } else if (signature instanceof TypeVariableSignature) {
            return TypeVariableModel
                    .resolveDependencies((TypeVariableSignature) signature);
        } else if (signature instanceof TypeArgument) {
            return TypeArgumentModel
                    .resolveDependencies((TypeArgument) signature);
        } else {
            return ClassRefSignatureModel
                    .resolveDependencies((ClassRefTypeSignature) signature);
        }
    }

    static Stream<Type> resolveDependencies(Type signature) {
        if (signature == null) {
            return Stream.empty();
        }

        if (signature instanceof ParameterizedType) {
            return ClassRefSignatureModel.resolveDependencies(signature);
        } else if (signature instanceof TypeVariable<?>) {
            return TypeVariableModel
                    .resolveDependencies((TypeVariable<?>) signature);
        } else if (signature instanceof WildcardType) {
            return TypeArgumentModel
                    .resolveDependencies((WildcardType) signature);
        } else if (signature instanceof GenericArrayType) {
            return ArraySignatureModel.resolveDependencies(signature);
        } else if (((Class<?>) signature).isPrimitive()) {
            return BaseSignatureModel.resolveDependencies(signature);
        } else if (((Class<?>) signature).isArray()) {
            return ArraySignatureModel.resolveDependencies(signature);
        } else {
            return ClassRefSignatureModel.resolveDependencies(signature);
        }
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

    default boolean isCharacter() {
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

    default boolean isJDKClass() {
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
