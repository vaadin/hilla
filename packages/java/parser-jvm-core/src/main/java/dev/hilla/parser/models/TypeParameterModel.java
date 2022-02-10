package dev.hilla.parser.models;

import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.TypeParameter;

public interface TypeParameterModel extends TypeModel {
    static TypeParameterModel of(@Nonnull TypeParameter origin,
            @Nonnull Dependable<?, ?> parent) {
        return new TypeParameterSourceModel(origin, parent);
    }

    static TypeParameterModel of(@Nonnull TypeVariable<?> origin,
            Dependable<?, ?> parent) {
        return new TypeParameterReflectionModel(origin, parent);
    }

    Collection<TypeModel> getBounds();

    default Stream<TypeModel> getBoundsStream() {
        return getBounds().stream();
    }

    @Override
    default boolean isTypeParameter() {
        return true;
    }
}
