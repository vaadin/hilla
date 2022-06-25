package dev.hilla.parser.models;

import java.util.List;

import javax.annotation.Nonnull;

abstract class MethodParameterInfoAbstractModel<T> extends AbstractModel<T>
        implements MethodParameterInfoModel {
    private List<AnnotationInfoModel> annotations;
    private MethodInfoModel owner;
    private SignatureModel type;

    MethodParameterInfoAbstractModel(@Nonnull T origin) {
        super(origin);
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
                && getModifiers() == other.getModifiers()
                && getType().equals(other.getType())
                && getName().equals(other.getName());
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = prepareAnnotations();
        }

        return annotations;
    }

    @Override
    public MethodInfoModel getOwner() {
        if (owner == null) {
            owner = prepareOwner();
        }

        return owner;
    }

    @Override
    public SignatureModel getType() {
        if (type == null) {
            type = prepareType();
        }

        return type;
    }

    @Override
    public int hashCode() {
        return getOwner().hashCodeIgnoreParameters()
                + 11 * getAnnotations().hashCode() + 17 * getModifiers()
                + 23 * getType().hashCode() + 53 * getName().hashCode();
    }

    protected abstract List<AnnotationInfoModel> prepareAnnotations();

    protected abstract MethodInfoModel prepareOwner();

    protected abstract SignatureModel prepareType();
}
