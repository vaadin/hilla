package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.TypeVariableSignature;

public abstract class TypeVariableModel extends AnnotatedAbstractModel
        implements SignatureModel, NamedModel {
    private TypeParameterModel typeParameter;

    public static TypeVariableModel of(@Nonnull TypeVariableSignature origin) {
        return new TypeVariableSourceModel(Objects.requireNonNull(origin));
    }

    public static TypeVariableModel of(@Nonnull AnnotatedTypeVariable origin) {
        return new TypeVariableReflectionModel(Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeVariableModel)) {
            return false;
        }

        var other = (TypeVariableModel) obj;

        return getName().equals(other.getName())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean isTypeVariable() {
        return true;
    }

    public SignatureModel resolve() {
        if (typeParameter == null) {
            typeParameter = prepareResolved();
        }

        return typeParameter;
    }

    protected abstract TypeParameterModel prepareResolved();
}
