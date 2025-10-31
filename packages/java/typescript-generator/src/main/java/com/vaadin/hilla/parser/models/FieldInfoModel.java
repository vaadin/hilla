package com.vaadin.hilla.parser.models;

import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.util.Objects;

import io.github.classgraph.FieldInfo;

public abstract class FieldInfoModel extends AnnotatedAbstractModel
        implements ClassMemberModel {
    private ClassInfoModel owner;
    private SignatureModel type;

    @Deprecated
    public static FieldInfoModel of(@NonNull FieldInfo origin) {
        return new FieldInfoSourceModel(Objects.requireNonNull(origin));
    }

    public static FieldInfoModel of(@NonNull Field origin) {
        return new FieldInfoReflectionModel(Objects.requireNonNull(origin));
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

    public abstract String getClassName();

    @Override
    public Class<FieldInfoModel> getCommonModelClass() {
        return FieldInfoModel.class;
    }

    @Override
    public ClassInfoModel getOwner() {
        if (owner == null) {
            owner = prepareOwner();
        }

        return owner;
    }

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

    public abstract boolean isEnum();

    public abstract boolean isFinal();

    public abstract boolean isPrivate();

    public abstract boolean isProtected();

    public abstract boolean isPublic();

    public abstract boolean isStatic();

    public abstract boolean isSynthetic();

    public abstract boolean isTransient();

    @Override
    public String toString() {
        return "FieldInfoModel[" + get() + "]";
    }

    protected abstract ClassInfoModel prepareOwner();

    protected abstract SignatureModel prepareType();
}
