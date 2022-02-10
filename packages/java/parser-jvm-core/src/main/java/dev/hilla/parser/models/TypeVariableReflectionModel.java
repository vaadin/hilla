package dev.hilla.parser.models;

import java.lang.reflect.TypeVariable;

final class TypeVariableReflectionModel
        extends AbstractReflectionSignatureDependable<TypeVariable<?>, Dependable<?, ?>>
        implements TypeVariableModel, ReflectionSignatureModel {
    private TypeParameterModel typeParameter;

    public TypeVariableReflectionModel(TypeVariable<?> origin,
            Dependable<?, ?> parent) {
        super(origin, parent);
    }

    @Override
    public TypeModel resolveDependencies() {
        if (typeParameter == null) {
            typeParameter = TypeParameterModel.of(origin, parent);
        }

        return typeParameter;
    }
}
