package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;

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

    SignatureModel getNestedType();

    @Override
    default boolean isArray() {
        return true;
    }
}
