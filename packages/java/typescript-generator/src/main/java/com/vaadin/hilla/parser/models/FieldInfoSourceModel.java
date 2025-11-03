package com.vaadin.hilla.parser.models;

import java.util.List;

import io.github.classgraph.FieldInfo;

final class FieldInfoSourceModel extends FieldInfoModel implements SourceModel {
    private final FieldInfo origin;

    FieldInfoSourceModel(FieldInfo origin) {
        this.origin = origin;
    }

    @Override
    public FieldInfo get() {
        return origin;
    }

    @Override
    public String getClassName() {
        return origin.getClassName();
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public boolean isEnum() {
        return origin.isEnum();
    }

    @Override
    public boolean isFinal() {
        return origin.isFinal();
    }

    @Override
    public boolean isPrivate() {
        return origin.isPrivate();
    }

    @Override
    public boolean isProtected() {
        return origin.isProtected();
    }

    @Override
    public boolean isPublic() {
        return origin.isPublic();
    }

    @Override
    public boolean isStatic() {
        return origin.isStatic();
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    public boolean isTransient() {
        return origin.isTransient();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotationInfo());
    }

    @Override
    protected ClassInfoModel prepareOwner() {
        return ClassInfoModel.of(origin.getClassInfo());
    }

    @Override
    protected SignatureModel prepareType() {
        return SignatureModel.of(origin.getTypeSignatureOrTypeDescriptor());
    }
}
