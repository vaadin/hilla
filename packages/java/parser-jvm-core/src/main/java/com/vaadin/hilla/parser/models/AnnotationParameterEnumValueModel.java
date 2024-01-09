package com.vaadin.hilla.parser.models;

import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationEnumValue;

public abstract class AnnotationParameterEnumValueModel implements Model {
    private ClassInfoModel classInfo;

    @Deprecated
    public static AnnotationParameterEnumValueModel of(
            @Nonnull AnnotationEnumValue origin) {
        return new AnnotationParameterEnumValueSourceModel(
                Objects.requireNonNull(origin));
    }

    public static AnnotationParameterEnumValueModel of(
            @Nonnull Enum<?> origin) {
        return new AnnotationParameterEnumValueReflectionModel(
                Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AnnotationParameterEnumValueModel)) {
            return false;
        }

        var other = (AnnotationParameterEnumValueModel) obj;

        return getClassInfo().equals(other.getClassInfo())
                && getValueName().equals(other.getValueName());
    }

    public ClassInfoModel getClassInfo() {
        if (classInfo == null) {
            classInfo = prepareClassInfo();
        }

        return classInfo;
    }

    @Override
    public Class<AnnotationParameterEnumValueModel> getCommonModelClass() {
        return AnnotationParameterEnumValueModel.class;
    }

    public abstract String getValueName();

    @Override
    public int hashCode() {
        return getClassInfo().hashCode() + 13 * getValueName().hashCode();
    }

    @Override
    public String toString() {
        return "AnnotationParameterEnumValueModel[" + get() + "]";
    }

    protected abstract ClassInfoModel prepareClassInfo();
}
