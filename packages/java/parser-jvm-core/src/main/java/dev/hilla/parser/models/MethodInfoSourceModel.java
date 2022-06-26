package dev.hilla.parser.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.classgraph.MethodInfo;

final class MethodInfoSourceModel extends MethodInfoModel
        implements SourceModel {
    private final MethodInfo origin;

    MethodInfoSourceModel(MethodInfo origin) {
        this.origin = origin;
    }

    @Override
    public boolean equalsIgnoreParameters(MethodInfoModel other) {
        return origin.getName().equals(other.getName())
                && origin.getModifiers() == other.getModifiers()
                && getResultType().equals(other.getResultType())
                && origin.getClassName().equals(other.getClassName());
    }

    @Override
    public MethodInfo get() {
        return origin;
    }

    @Override
    public String getClassName() {
        return origin.getClassName();
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
    public int hashCodeIgnoreParameters() {
        return origin.getName().hashCode() + 11 * getResultType().hashCode()
                + 17 * origin.getModifiers()
                + 23 * origin.getClassName().hashCode();
    }

    @Override
    public boolean isAbstract() {
        return origin.isAbstract();
    }

    @Override
    public boolean isBridge() {
        return origin.isBridge();
    }

    @Override
    public boolean isFinal() {
        return origin.isFinal();
    }

    @Override
    public boolean isNative() {
        return origin.isNative();
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
    public boolean isStrict() {
        return origin.isStrict();
    }

    @Override
    public boolean isSynchronized() {
        return origin.isSynchronized();
    }

    @Override
    public boolean isSynthetic() {
        return origin.isSynthetic();
    }

    @Override
    public boolean isVarArgs() {
        return origin.isVarArgs();
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
    protected List<MethodParameterInfoModel> prepareParameters() {
        return Arrays.stream(origin.getParameterInfo())
                .map(MethodParameterInfoModel::of).collect(Collectors.toList());
    }

    @Override
    protected SignatureModel prepareResultType() {
        return SignatureModel
                .of(origin.getTypeSignatureOrTypeDescriptor().getResultType());
    }
}
