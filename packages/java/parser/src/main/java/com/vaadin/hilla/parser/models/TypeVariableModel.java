package com.vaadin.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import io.github.classgraph.TypeVariableSignature;

public abstract class TypeVariableModel extends AnnotatedAbstractModel
        implements SignatureModel, NamedModel {
    private TypeParameterModel typeParameter;

    @Deprecated
    public static TypeVariableModel of(@NonNull TypeVariableSignature origin) {
        return new TypeVariableSourceModel(Objects.requireNonNull(origin));
    }

    public static TypeVariableModel of(@NonNull AnnotatedTypeVariable origin) {
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
                && getAnnotations().equals(other.getAnnotations())
                && resolve().equals(other.resolve());
    }

    @Override
    public Class<TypeVariableModel> getCommonModelClass() {
        return TypeVariableModel.class;
    }

    @Override
    public int hashCode() {
        return 0x4f76c9f1 ^ getName().hashCode();
    }

    @Override
    public boolean isTypeVariable() {
        return true;
    }

    public TypeParameterModel resolve() {
        if (typeParameter == null) {
            typeParameter = prepareResolved();
        }

        return typeParameter;
    }

    @Override
    public String toString() {
        return "TypeVariableModel[" + get() + "]";
    }

    protected abstract TypeParameterModel prepareResolved();
}
