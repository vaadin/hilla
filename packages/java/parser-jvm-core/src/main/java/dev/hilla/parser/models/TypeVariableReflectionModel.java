package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.TypeVariable;
import java.util.List;

final class TypeVariableReflectionModel
        extends TypeVariableAbstractModel<AnnotatedTypeVariable>
        implements ReflectionSignatureModel {

    TypeVariableReflectionModel(AnnotatedTypeVariable origin) {
        super(origin);
    }

    @Override
    public String getName() {
        return ((TypeVariable<?>) origin.getType()).getName();
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
