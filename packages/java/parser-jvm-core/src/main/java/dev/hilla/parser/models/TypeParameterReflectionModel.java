package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class TypeParameterReflectionModel
        extends AbstractAnnotatedReflectionModel<AnnotatedTypeVariable>
        implements TypeParameterModel, ReflectionSignatureModel {
    private List<SignatureModel> bounds;

    public TypeParameterReflectionModel(AnnotatedTypeVariable origin,
            Model parent) {
        super(origin, Objects.requireNonNull(parent));
    }

    @Override
    public List<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = Arrays.stream(origin.getAnnotatedBounds())
                    .map(signature -> SignatureModel.of(signature, this))
                    .collect(Collectors.toList());
        }

        return bounds;
    }
}
