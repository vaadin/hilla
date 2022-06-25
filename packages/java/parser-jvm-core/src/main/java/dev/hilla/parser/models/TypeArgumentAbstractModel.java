package dev.hilla.parser.models;

import java.util.List;

import javax.annotation.Nonnull;

abstract class TypeArgumentAbstractModel<T> extends AbstractModel<T>
        implements TypeArgumentModel {
    private List<AnnotationInfoModel> annotations;
    private List<SignatureModel> associatedTypes;

    TypeArgumentAbstractModel(@Nonnull T origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeArgumentModel)) {
            return false;
        }

        var other = (TypeArgumentModel) obj;

        return getAnnotations().equals(other.getAnnotations())
                && getAssociatedTypes().equals(other.getAssociatedTypes())
                && getWildcard().equals(other.getWildcard());
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = prepareAnnotations();
        }

        return annotations;
    }

    @Override
    public List<SignatureModel> getAssociatedTypes() {
        if (associatedTypes == null) {
            associatedTypes = prepareAssociatedTypes();
        }

        return associatedTypes;
    }

    @Override
    public int hashCode() {
        return getAssociatedTypes().hashCode() + 7 * getWildcard().hashCode();
    }

    protected abstract List<AnnotationInfoModel> prepareAnnotations();

    protected abstract List<SignatureModel> prepareAssociatedTypes();
}
