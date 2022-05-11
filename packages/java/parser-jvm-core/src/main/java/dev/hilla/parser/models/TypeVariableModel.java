package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.TypeVariableSignature;

public interface TypeVariableModel extends SignatureModel {
    static TypeVariableModel of(@Nonnull TypeVariableSignature origin,
            @Nonnull Model parent) {
        return new TypeVariableSourceModel(Objects.requireNonNull(origin),
                Objects.requireNonNull(parent));
    }

    static TypeVariableModel of(@Nonnull AnnotatedTypeVariable origin,
            Model parent) {
        return new TypeVariableReflectionModel(Objects.requireNonNull(origin),
                parent);
    }

    @Override
    default boolean isTypeVariable() {
        return true;
    }

    SignatureModel resolve();
}
