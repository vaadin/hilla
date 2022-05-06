package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;

final class TypeVariableReflectionModel
        extends AbstractAnnotatedReflectionModel<AnnotatedTypeVariable>
        implements TypeVariableModel, ReflectionSignatureModel {
    private TypeParameterModel typeParameter;

    public TypeVariableReflectionModel(AnnotatedTypeVariable origin) {
        super(origin);
    }

    @Override
    public SignatureModel resolve() {
        if (typeParameter == null) {
            typeParameter = TypeParameterModel.of(origin);
        }

        return typeParameter;
    }
}
