package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.BaseTypeSignature;

public interface BaseSignatureModel extends SignatureModel {
    static BaseSignatureModel of(@Nonnull BaseTypeSignature origin) {
        return new BaseSignatureSourceModel(Objects.requireNonNull(origin));
    }

    static BaseSignatureModel of(@Nonnull AnnotatedType origin) {
        return new BaseSignatureReflectionModel(Objects.requireNonNull(origin));
    }

    static BaseSignatureModel of(@Nonnull Class<?> origin) {
        return new BaseSignatureReflectionModel.Bare(
                Objects.requireNonNull(origin));
    }

    Class<?> getType();

    @Override
    default boolean isBase() {
        return true;
    }
}
