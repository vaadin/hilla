package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class TypeParameterReflectionModel
        extends AbstractModel<AnnotatedTypeVariable>
        implements TypeParameterModel, ReflectionSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private List<SignatureModel> bounds;

    public TypeParameterReflectionModel(AnnotatedTypeVariable origin,
            Model parent) {
        super(origin, Objects.requireNonNull(parent));
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = Arrays.stream(origin.getAnnotations())
                .map(annotation -> AnnotationInfoModel.of(annotation, this))
                .collect(Collectors.toList());
        }

        return annotations;
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
