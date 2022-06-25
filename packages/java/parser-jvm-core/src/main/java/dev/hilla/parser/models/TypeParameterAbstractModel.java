package dev.hilla.parser.models;

import java.util.List;

import javax.annotation.Nonnull;

abstract class TypeParameterAbstractModel<T> extends AnnotatedAbstractModel<T>
        implements TypeParameterModel {
    private List<SignatureModel> bounds;

    TypeParameterAbstractModel(@Nonnull T origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeParameterModel)) {
            return false;
        }

        var other = (TypeParameterModel) obj;

        return getName().equals(other.getName())
                && getAnnotations().equals(other.getAnnotations())
                && getBounds().equals(other.getBounds());
    }

    @Override
    public List<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = prepareBounds();
        }

        return bounds;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 3 * getBounds().hashCode();
    }

    protected abstract List<SignatureModel> prepareBounds();
}
