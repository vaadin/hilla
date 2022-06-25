package dev.hilla.parser.models;

import java.util.List;

import javax.annotation.Nonnull;

abstract class FieldInfoAbstractModel<T> extends AnnotatedAbstractModel<T>
        implements FieldInfoModel {
    private ClassInfoModel owner;
    private SignatureModel type;

    FieldInfoAbstractModel(@Nonnull T origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FieldInfoModel)) {
            return false;
        }

        var other = (FieldInfoModel) obj;

        return getClassName().equals(other.getClassName())
                && getName().equals(other.getName());
    }

    @Override
    public ClassInfoModel getOwner() {
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
        return getName().hashCode() + 11 * getClassName().hashCode();
    }

    protected abstract ClassInfoModel prepareOwner();

    protected abstract SignatureModel prepareType();
}
