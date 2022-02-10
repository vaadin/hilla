package dev.hilla.parser.models;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class TypeParameterReflectionModel extends
        AbstractReflectionSignatureDependable<TypeVariable<?>, Dependable<?, ?>>
        implements TypeParameterModel, ReflectionSignatureModel {
    private Collection<TypeModel> bounds;

    public TypeParameterReflectionModel(TypeVariable<?> origin,
            Dependable<?, ?> parent) {
        super(origin, Objects.requireNonNull(parent));
    }

    @Override
    public Collection<TypeModel> getBounds() {
        if (bounds == null) {
            bounds = Arrays.stream(origin.getBounds()).map(
                    signature -> ReflectionSignatureModel.of(signature, this))
                    .collect(Collectors.toList());
        }

        return bounds;
    }
}
