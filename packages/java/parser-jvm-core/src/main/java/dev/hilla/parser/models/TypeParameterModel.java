package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.TypeParameter;

public interface TypeParameterModel extends SignatureModel {
    static TypeParameterModel of(@Nonnull TypeParameter origin) {
        return new TypeParameterSourceModel(origin);
    }

    static TypeParameterModel of(@Nonnull AnnotatedTypeVariable origin) {
        return new TypeParameterReflectionModel(origin);
    }

    List<SignatureModel> getBounds();

    default Stream<SignatureModel> getBoundsStream() {
        return getBounds().stream();
    }

    String getName();

    @Override
    default boolean isTypeParameter() {
        return true;
    }
}
