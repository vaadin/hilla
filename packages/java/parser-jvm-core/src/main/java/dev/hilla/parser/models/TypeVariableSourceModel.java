package dev.hilla.parser.models;

import java.util.List;

import io.github.classgraph.TypeVariableSignature;

final class TypeVariableSourceModel
        extends TypeVariableAbstractModel<TypeVariableSignature>
        implements SourceSignatureModel {
    TypeVariableSourceModel(TypeVariableSignature origin) {
        super(origin);
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getTypeAnnotationInfo());
    }

    @Override
    protected TypeParameterModel prepareResolved() {
        return TypeParameterModel.of(origin.resolve());
    }
}
