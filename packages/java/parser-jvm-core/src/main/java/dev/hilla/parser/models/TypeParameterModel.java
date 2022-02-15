package dev.hilla.parser.models;

import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.TypeParameter;

public interface TypeParameterModel extends SignatureModel {
    static TypeParameterModel of(@Nonnull TypeParameter origin,
            @Nonnull Model parent) {
        return new TypeParameterSourceModel(origin, parent);
    }

    static TypeParameterModel of(@Nonnull TypeVariable<?> origin,
            Model parent) {
        return new TypeParameterReflectionModel(origin, parent);
    }

    Collection<SignatureModel> getBounds();

    default Stream<SignatureModel> getBoundsStream() {
        return getBounds().stream();
    }

    @Override
    default boolean isTypeParameter() {
        return true;
    }
}
