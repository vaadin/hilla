package dev.hilla.parser.models;

import java.lang.reflect.TypeVariable;

final class TypeVariableReflectionModel
        extends AbstractReflectionSignatureModel<TypeVariable<?>>
        implements TypeVariableModel, ReflectionModel {
    private TypeParameterModel typeParameter;

    public TypeVariableReflectionModel(TypeVariable<?> origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public SignatureModel resolve() {
        if (typeParameter == null) {
            typeParameter = TypeParameterModel.of(origin, parent);
        }

        return typeParameter;
    }
}
