package dev.hilla.parser.models;

import io.github.classgraph.TypeVariableSignature;

final class TypeVariableSourceModel extends
        AbstractSourceSignatureDependable<TypeVariableSignature, Dependable<?, ?>>
        implements TypeVariableModel, SourceSignatureModel {
    private TypeParameterModel typeParameter;

    public TypeVariableSourceModel(TypeVariableSignature origin,
            Dependable<?, ?> parent) {
        super(origin, parent);
    }

    public TypeParameterModel resolveDependencies() {
        if (typeParameter == null) {
            typeParameter = TypeParameterModel.of(origin.resolve(), this);
        }

        return typeParameter;
    }
}
