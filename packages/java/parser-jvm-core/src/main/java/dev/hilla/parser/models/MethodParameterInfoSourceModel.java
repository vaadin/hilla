package dev.hilla.parser.models;

import java.util.List;

import io.github.classgraph.MethodParameterInfo;

final class MethodParameterInfoSourceModel
        extends MethodParameterInfoAbstractModel<MethodParameterInfo>
        implements MethodParameterInfoModel, SourceModel {
    public MethodParameterInfoSourceModel(MethodParameterInfo parameter) {
        super(parameter);
    }

    @Override
    public int getModifiers() {
        return origin.getModifiers();
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public boolean isFinal() {
        return origin.isFinal();
    }

    @Override
    public boolean isMandated() {
        return origin.isMandated();
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return AnnotationUtils.convert(origin.getAnnotationInfo());
    }

    @Override
    protected MethodInfoModel prepareOwner() {
        return MethodInfoModel.of(origin.getMethodInfo());
    }

    @Override
    protected SignatureModel prepareType() {
        return SignatureModel.of(origin.getTypeSignatureOrTypeDescriptor());
    }
}
