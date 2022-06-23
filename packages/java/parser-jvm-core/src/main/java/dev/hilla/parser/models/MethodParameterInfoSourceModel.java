package dev.hilla.parser.models;

import java.util.stream.Stream;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.MethodParameterInfo;

final class MethodParameterInfoSourceModel
        extends AbstractAnnotatedSourceModel<MethodParameterInfo>
        implements MethodParameterInfoModel, SourceModel {
    private MethodInfoModel owner;
    private SignatureModel type;

    public MethodParameterInfoSourceModel(MethodParameterInfo parameter) {
        super(parameter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MethodParameterInfoModel)) {
            return false;
        }

        var other = (MethodParameterInfoModel) obj;

        return getOwner().equalsIgnoreParameters(other.getOwner())
                && getAnnotations().equals(other.getAnnotations())
                && origin.getModifiers() == other.getModifiers()
                && getType().equals(other.getType())
                && origin.getName().equals(other.getName());
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
    public MethodInfoModel getOwner() {
        if (owner == null) {
            owner = MethodInfoModel.of(origin.getMethodInfo());
        }

        return owner;
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = SignatureModel.of(origin.getTypeSignatureOrTypeDescriptor());
        }

        return type;
    }

    @Override
    public int hashCode() {
        return getOwner().hashCodeIgnoreParameters()
                + 11 * getAnnotations().hashCode() + 17 * origin.getModifiers()
                + 23 * getType().hashCode() + 53 * origin.getName().hashCode();
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
    protected Stream<AnnotationInfo> getOriginAnnotations() {
        return origin.getAnnotationInfo().stream();
    }
}
