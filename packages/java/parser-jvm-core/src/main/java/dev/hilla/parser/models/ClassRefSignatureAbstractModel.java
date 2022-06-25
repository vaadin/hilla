package dev.hilla.parser.models;

import java.util.List;

import javax.annotation.Nonnull;

abstract class ClassRefSignatureAbstractModel<T> extends AnnotatedAbstractModel<T>
        implements ClassRefSignatureModel {
    private ClassInfoModel reference;
    private List<TypeArgumentModel> typeArguments;

    ClassRefSignatureAbstractModel(@Nonnull T origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ClassRefSignatureModel)) {
            return false;
        }

        var other = (ClassRefSignatureModel) obj;

        return getClassName().equals(other.getClassName())
                && getOwner().equals(other.getOwner())
                && getTypeArguments().equals(other.getTypeArguments())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public ClassInfoModel getClassInfo() {
        if (reference == null) {
            reference = prepareClassInfo();
        }

        return reference;
    }

    @Override
    public List<TypeArgumentModel> getTypeArguments() {
        if (typeArguments == null) {
            typeArguments = prepareTypeArguments();
        }

        return typeArguments;
    }

    @Override
    public int hashCode() {
        return getClassName().hashCode() + 7 * getTypeArguments().hashCode()
                + 23 * getAnnotations().hashCode() + 53 * getOwner().hashCode();
    }

    @Override
    public void setReference(ClassInfoModel reference) {
        this.reference = reference;
    }

    protected abstract List<AnnotationInfoModel> prepareAnnotations();

    protected abstract ClassInfoModel prepareClassInfo();

    protected abstract List<TypeArgumentModel> prepareTypeArguments();
}
