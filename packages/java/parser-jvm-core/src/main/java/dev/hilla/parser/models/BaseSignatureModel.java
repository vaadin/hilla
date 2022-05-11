package dev.hilla.parser.models;

import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.BaseTypeSignature;

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

    @Override
    default boolean isBase() {
        return true;
    }
}
