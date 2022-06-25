package dev.hilla.parser.models;

import java.util.List;

import javax.annotation.Nonnull;

abstract class ArraySignatureAbstractModel<T> extends AnnotatedAbstractModel<T>
        implements ArraySignatureModel {
    private SignatureModel nestedType;

    ArraySignatureAbstractModel(@Nonnull T origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ArraySignatureModel)) {
            return false;
        }

        var other = (ArraySignatureModel) obj;

        return getNestedType().equals(other.getNestedType())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public SignatureModel getNestedType() {
        if (nestedType == null) {
            nestedType = prepareNestedType();
        }

        return nestedType;
    }

    @Override
    public int hashCode() {
        return 1 + getNestedType().hashCode();
    }

    protected abstract SignatureModel prepareNestedType();
}
