package com.vaadin.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import io.github.classgraph.AnnotationInfo;

public abstract class AnnotationInfoModel implements Model, NamedModel {
    private Optional<ClassInfoModel> classInfo;
    private Set<AnnotationParameterModel> parameters;

    @Deprecated
    public static AnnotationInfoModel of(@NonNull AnnotationInfo origin) {
        return new AnnotationInfoSourceModel(Objects.requireNonNull(origin));
    }

    public static AnnotationInfoModel of(@NonNull Annotation origin) {
        return new AnnotationInfoReflectionModel(
                Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AnnotationInfoModel)) {
            return false;
        }

        var other = (AnnotationInfoModel) obj;

        return getName().equals(other.getName())
                && getParameters().equals(other.getParameters());
    }

    public Optional<ClassInfoModel> getClassInfo() {
        if (classInfo == null) {
            classInfo = prepareClassInfo();
        }

        return classInfo;
    }

    @Override
    public Class<AnnotationInfoModel> getCommonModelClass() {
        return AnnotationInfoModel.class;
    }

    public Set<AnnotationParameterModel> getParameters() {
        if (parameters == null) {
            parameters = prepareParameters();
        }

        return parameters;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 11 * getParameters().hashCode();
    }

    @Override
    public String toString() {
        return "AnnotationInfoModel[" + get() + "]";
    }

    protected abstract Optional<ClassInfoModel> prepareClassInfo();

    protected abstract Set<AnnotationParameterModel> prepareParameters();
}
