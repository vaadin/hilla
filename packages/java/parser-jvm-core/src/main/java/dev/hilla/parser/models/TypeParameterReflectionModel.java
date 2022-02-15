package dev.hilla.parser.models;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

final class TypeParameterReflectionModel
        extends AbstractReflectionSignatureModel<TypeVariable<?>>
        implements TypeParameterModel, ReflectionModel {
    private Collection<SignatureModel> bounds;

    public TypeParameterReflectionModel(TypeVariable<?> origin, Model parent) {
        super(origin, Objects.requireNonNull(parent));
    }

    @Override
    public Collection<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = Arrays.stream(origin.getBounds()).map(
                    signature -> SignatureModel.of(signature, this))
                    .collect(Collectors.toList());
        }

        return bounds;
    }
}
