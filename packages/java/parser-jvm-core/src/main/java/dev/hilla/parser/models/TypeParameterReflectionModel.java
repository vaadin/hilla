package dev.hilla.parser.models;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class TypeParameterReflectionModel extends TypeParameterModel
        implements ReflectionSignatureModel {
    private final TypeVariable<?> origin;

    TypeParameterReflectionModel(TypeVariable<?> origin) {
        this.origin = origin;
    }

    @Override
    public TypeVariable<?> get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        // TODO: Temporary limitation because ClassGraph doesn't provide stored
        // annotations for TypeParameter.
        return List.of();
    }

    @Override
    protected List<SignatureModel> prepareBounds() {
        return Arrays.stream(origin.getAnnotatedBounds())
                .map(SignatureModel::of).collect(Collectors.toList());
    }
}
