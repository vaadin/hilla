package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;
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

    static ArraySignatureModel of(@Nonnull AnnotatedArrayType origin,
            Model parent) {
        return new ArraySignatureReflectionModel(Objects.requireNonNull(origin),
                parent);
    }

    static Stream<ClassInfo> resolveDependencies(
            @Nonnull ArrayTypeSignature signature) {
        return SignatureModel.resolveDependencies(
                Objects.requireNonNull(signature).getElementTypeSignature());
    }

    static Stream<Class<?>> resolveDependencies(
            @Nonnull AnnotatedArrayType signature) {
        return SignatureModel.resolveDependencies(
                signature.getAnnotatedGenericComponentType());
    }

    SignatureModel getNestedType();

    @Override
    default boolean isArray() {
        return true;
    }
}
