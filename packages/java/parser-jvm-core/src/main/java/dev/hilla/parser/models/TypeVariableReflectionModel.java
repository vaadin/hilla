package dev.hilla.parser.models;

import java.lang.reflect.TypeVariable;
import java.util.List;

final class TypeVariableReflectionModel extends TypeVariableModel
        implements ReflectionSignatureModel {
    private final TypeVariable<?> origin;

    TypeVariableReflectionModel(TypeVariable<?> origin) {
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
        return processAnnotations(origin.getAnnotations());
    }

    @Override
    protected TypeParameterModel prepareResolved() {
        return TypeParameterModel.of(origin);
    }
}
