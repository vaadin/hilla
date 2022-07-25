package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationInfo;

public abstract class AnnotationInfoModel implements Model, NamedModel {
    private ClassInfoModel classInfo;
    private Set<AnnotationParameterModel> parameters;

    public static AnnotationInfoModel of(@Nonnull AnnotationInfo origin) {
        return new AnnotationInfoSourceModel(Objects.requireNonNull(origin));
    }

    public static AnnotationInfoModel of(@Nonnull Annotation origin) {
        return new AnnotationInfoReflectionModel(Objects.requireNonNull(origin));
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

    public ClassInfoModel getClassInfo() {
        if (classInfo == null) {
            classInfo = prepareClassInfo();
        }

        return classInfo;
    }

    @Override
    public Stream<ClassInfoModel> getDependenciesStream() {
        return getClassInfo().getDependenciesStream();
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

    protected abstract ClassInfoModel prepareClassInfo();

    protected abstract Set<AnnotationParameterModel> prepareParameters();
}
