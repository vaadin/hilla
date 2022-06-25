package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class TypeParameterReflectionModel
        extends TypeParameterAbstractModel<AnnotatedTypeVariable>
        implements ReflectionSignatureModel {
    private List<SignatureModel> bounds;

    public TypeParameterReflectionModel(AnnotatedTypeVariable origin) {
        super(origin);
    }

    @Override
    public String getName() {
        return ((TypeVariable<?>) origin.getType()).getName();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return AnnotationUtils.convert(origin.getAnnotations());
    }

    @Override
    protected List<SignatureModel> prepareBounds() {
        return Arrays.stream(origin.getAnnotatedBounds())
                .map(SignatureModel::of).collect(Collectors.toList());
    }
}
