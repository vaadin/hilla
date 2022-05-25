package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.TypeVariableSignature;

public interface TypeVariableModel extends SignatureModel {
    static TypeVariableModel of(@Nonnull TypeVariableSignature origin) {
        return new TypeVariableSourceModel(Objects.requireNonNull(origin));
    }

    static TypeVariableModel of(@Nonnull AnnotatedTypeVariable origin) {
        return new TypeVariableReflectionModel(Objects.requireNonNull(origin));
    }

    String getName();

    @Override
    default boolean isTypeVariable() {
        return true;
    }

    SignatureModel resolve();
}
