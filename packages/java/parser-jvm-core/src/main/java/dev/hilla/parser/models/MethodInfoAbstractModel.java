package dev.hilla.parser.models;

import java.util.List;

import javax.annotation.Nonnull;

abstract class MethodInfoAbstractModel<T> extends AbstractModel<T>
        implements MethodInfoModel {
    private List<AnnotationInfoModel> annotations;
    private ClassInfoModel owner;
    private List<MethodParameterInfoModel> parameters;
    private SignatureModel resultType;

    MethodInfoAbstractModel(@Nonnull T origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MethodInfoModel)) {
            return false;
        }

        var other = (MethodInfoModel) obj;

        return equalsIgnoreParameters(other)
                && getParameters().equals(other.getParameters());
    }

    @Override
    public boolean equalsIgnoreParameters(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MethodInfoModel)) {
            return false;
        }

        return equalsIgnoreParameters((MethodInfoModel) obj);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = prepareAnnotations();
        }

        return annotations;
    }

    @Override
    public ClassInfoModel getOwner() {
        if (owner == null) {
            owner = prepareOwner();
        }

        return owner;
    }

    @Override
    public List<MethodParameterInfoModel> getParameters() {
        if (parameters == null) {
            parameters = prepareParameters();
        }

        return parameters;
    }

    @Override
    public SignatureModel getResultType() {
        if (resultType == null) {
            resultType = prepareResultType();
        }

        return resultType;
    }

    @Override
    public int hashCode() {
        return hashCodeIgnoreParameters() + 53 * getParameters().hashCode();
    }

    protected abstract List<AnnotationInfoModel> prepareAnnotations();

    protected abstract ClassInfoModel prepareOwner();

    protected abstract List<MethodParameterInfoModel> prepareParameters();

    protected abstract SignatureModel prepareResultType();
}
