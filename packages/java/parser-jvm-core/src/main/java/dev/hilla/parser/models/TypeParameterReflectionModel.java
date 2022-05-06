package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class TypeParameterReflectionModel
        extends AbstractAnnotatedReflectionModel<AnnotatedTypeVariable>
        implements TypeParameterModel, ReflectionSignatureModel {
    private List<SignatureModel> bounds;

    public TypeParameterReflectionModel(AnnotatedTypeVariable origin) {
        super(origin);
    }

    @Override
    public List<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = Arrays.stream(origin.getAnnotatedBounds())
                    .map(SignatureModel::of).collect(Collectors.toList());
        }

        return bounds;
    }
}
