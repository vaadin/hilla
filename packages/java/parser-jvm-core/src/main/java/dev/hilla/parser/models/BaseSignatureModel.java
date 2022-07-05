package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.BaseTypeSignature;

public abstract class BaseSignatureModel extends AnnotatedAbstractModel
        implements SignatureModel {
    public static BaseSignatureModel of(@Nonnull BaseTypeSignature origin) {
        return new BaseSignatureSourceModel(Objects.requireNonNull(origin));
    }

    public static BaseSignatureModel of(@Nonnull AnnotatedType origin) {
        return new BaseSignatureReflectionModel(Objects.requireNonNull(origin));
    }

    public static BaseSignatureModel of(@Nonnull Class<?> origin) {
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

    public abstract Class<?> getType();

    @Override
    public int hashCode() {
        return 7 + getType().hashCode();
    }

    @Override
    public boolean isBase() {
        return true;
    }
}
