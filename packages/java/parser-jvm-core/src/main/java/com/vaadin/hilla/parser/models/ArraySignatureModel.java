package com.vaadin.hilla.parser.models;

import java.lang.reflect.AnnotatedArrayType;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;

public abstract class ArraySignatureModel extends AnnotatedAbstractModel
        implements SignatureModel {
    private SignatureModel nestedType;

    @Deprecated
    public static ArraySignatureModel of(@Nonnull ArrayTypeSignature origin) {
        return new ArraySignatureSourceModel(Objects.requireNonNull(origin));
    }

    public static ArraySignatureModel of(@Nonnull AnnotatedArrayType origin) {
        return new ArraySignatureReflectionModel(
                Objects.requireNonNull(origin));
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
    public Class<ArraySignatureModel> getCommonModelClass() {
        return ArraySignatureModel.class;
    }

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

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public String toString() {
        return "ArraySignatureModel[" + get() + "]";
    }

    protected abstract SignatureModel prepareNestedType();
}
