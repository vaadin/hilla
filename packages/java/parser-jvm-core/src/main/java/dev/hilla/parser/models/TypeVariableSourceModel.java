package dev.hilla.parser.models;

import io.github.classgraph.TypeVariableSignature;

final class TypeVariableSourceModel extends AbstractModel<TypeVariableSignature>
        implements TypeVariableModel, SourceSignatureModel {
    private TypeParameterModel typeParameter;

    public TypeVariableSourceModel(TypeVariableSignature origin, Model parent) {
        super(origin, parent);
    }

    public TypeParameterModel resolve() {
        if (typeParameter == null) {
            typeParameter = TypeParameterModel.of(origin.resolve(), this);
        }

        return typeParameter;
    }
}
