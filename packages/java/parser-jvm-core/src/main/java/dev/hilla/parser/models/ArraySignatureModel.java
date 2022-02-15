package dev.hilla.parser.models;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.ClassInfo;

public interface ArraySignatureModel extends SignatureModel {
    static ArraySignatureModel of(@Nonnull ArrayTypeSignature origin,
            @Nonnull Model parent) {
        return new ArraySignatureSourceModel(Objects.requireNonNull(origin),
                Objects.requireNonNull(parent));
    }

    static ArraySignatureModel of(@Nonnull Type origin, Model parent) {
        if (origin instanceof GenericArrayType || (origin instanceof Class<?>
                && ((Class<?>) origin).isArray())) {
            return new ArraySignatureReflectionModel(
                    Objects.requireNonNull(origin), parent);
        }

        throw new IllegalArgumentException(
                ArraySignatureReflectionModel.ILLEGAL_ARGUMENTS_EXCEPTION_MSG);
    }

    static Stream<ClassInfo> resolveDependencies(
            @Nonnull ArrayTypeSignature signature) {
        return SignatureModel.resolveDependencies(
                Objects.requireNonNull(signature).getElementTypeSignature());
    }

    static Stream<Type> resolveDependencies(@Nonnull Type signature) {
        Type componentType;

        if (Objects.requireNonNull(signature) instanceof GenericArrayType) {
            componentType = ((GenericArrayType) signature)
                    .getGenericComponentType();
        } else if ((signature instanceof Class<?>
                && ((Class<?>) signature).isArray())) {
            componentType = ((Class<?>) signature).getComponentType();
        } else {
            throw new IllegalArgumentException(
                    ArraySignatureReflectionModel.ILLEGAL_ARGUMENTS_EXCEPTION_MSG);
        }

        return SignatureModel.resolveDependencies(componentType);
    }

    SignatureModel getNestedType();

    @Override
    default boolean isArray() {
        return true;
    }
}
