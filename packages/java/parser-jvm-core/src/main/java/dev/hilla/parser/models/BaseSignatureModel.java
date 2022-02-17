package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassInfo;

public interface BaseSignatureModel extends SignatureModel {
    static BaseSignatureModel of(@Nonnull BaseTypeSignature origin,
            @Nonnull Model parent) {
        return new BaseSignatureSourceModel(Objects.requireNonNull(origin),
                Objects.requireNonNull(parent));
    }

    static BaseSignatureModel of(@Nonnull Class<?> origin, Model parent) {
        return new BaseSignatureReflectionModel(Objects.requireNonNull(origin),
                parent);
    }

    static Stream<ClassInfo> resolveDependencies(
            @Nonnull BaseTypeSignature signature) {
        // BaseType is about primitive types (int, double, etc.).
        // We don't need to resolve them, so skipping.
        return Stream.empty();
    }

    static Stream<Class<?>> resolveDependencies(
            @Nonnull AnnotatedElement signature) {
        // BaseType is about primitive types (int, double, etc.).
        // We don't need to resolve them, so skipping.
        return Stream.empty();
    }

    @Override
    default boolean isBase() {
        return true;
    }
}
