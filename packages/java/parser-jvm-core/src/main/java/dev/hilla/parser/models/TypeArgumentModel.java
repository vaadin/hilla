package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.TypeArgument;

public interface TypeArgumentModel extends SignatureModel {
    static TypeArgumentModel of(@Nonnull TypeArgument origin) {
        return new TypeArgumentSourceModel(Objects.requireNonNull(origin));
    }

    static TypeArgumentModel of(@Nonnull AnnotatedType origin) {
        return new TypeArgumentReflectionModel(Objects.requireNonNull(origin));
    }

    List<SignatureModel> getAssociatedTypes();

    TypeArgument.Wildcard getWildcard();

    @Override
    default boolean isTypeArgument() {
        return true;
    }
}
