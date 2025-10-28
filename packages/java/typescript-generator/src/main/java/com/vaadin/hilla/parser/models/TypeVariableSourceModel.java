package com.vaadin.hilla.parser.models;

import java.util.List;

import io.github.classgraph.TypeVariableSignature;

final class TypeVariableSourceModel extends TypeVariableModel
        implements SourceSignatureModel {
    private final TypeVariableSignature origin;

    TypeVariableSourceModel(TypeVariableSignature origin) {
        this.origin = origin;
    }

    @Override
    public TypeVariableSignature get() {
        return origin;
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
