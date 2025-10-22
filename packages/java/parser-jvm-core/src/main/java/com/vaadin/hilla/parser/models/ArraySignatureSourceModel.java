package com.vaadin.hilla.parser.models;

import java.util.List;

import io.github.classgraph.ArrayTypeSignature;

final class ArraySignatureSourceModel extends ArraySignatureModel
        implements SourceSignatureModel {
    private final ArrayTypeSignature origin;

    ArraySignatureSourceModel(ArrayTypeSignature origin) {
        this.origin = origin;
    }

    @Override
    public ArrayTypeSignature get() {
        return origin;
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getTypeAnnotationInfo());
    }

    @Override
    protected SignatureModel prepareNestedType() {
        return SignatureModel.of(origin.getNestedType());
    }
}
