package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;

public interface ArraySignatureModel extends SignatureModel {
    static ArraySignatureModel of(@Nonnull ArrayTypeSignature origin) {
        return new ArraySignatureSourceModel(Objects.requireNonNull(origin));
    }

    static ArraySignatureModel of(@Nonnull AnnotatedArrayType origin) {
        return new ArraySignatureReflectionModel(
                Objects.requireNonNull(origin));
    }

    SignatureModel getNestedType();

    @Override
    default boolean isArray() {
        return true;
    }
}
