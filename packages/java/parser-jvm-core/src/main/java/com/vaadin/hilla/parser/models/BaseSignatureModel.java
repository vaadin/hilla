package com.vaadin.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import io.github.classgraph.BaseTypeSignature;

public abstract class BaseSignatureModel extends AnnotatedAbstractModel
        implements SignatureModel {
    @Deprecated
    public static BaseSignatureModel of(@NonNull BaseTypeSignature origin) {
        return new BaseSignatureSourceModel(Objects.requireNonNull(origin));
    }

    public static BaseSignatureModel of(@NonNull AnnotatedType origin) {
        return new BaseSignatureReflectionModel(Objects.requireNonNull(origin));
    }

    public static BaseSignatureModel of(@NonNull Class<?> origin) {
        return new BaseSignatureReflectionModel.Bare(
                Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof BaseSignatureModel)) {
            return false;
        }

        var other = (BaseSignatureModel) obj;

        return getType().equals(other.getType())
                && Objects.equals(getAnnotations(), other.getAnnotations());
    }

    @Override
    public Class<BaseSignatureModel> getCommonModelClass() {
        return BaseSignatureModel.class;
    }

    public abstract Class<?> getType();

    @Override
    public int hashCode() {
        return 7 + getType().hashCode();
    }

    @Override
    public boolean isBase() {
        return true;
    }

    @Override
    public String toString() {
        return "BaseSignatureModel[" + get() + "]";
    }
}
