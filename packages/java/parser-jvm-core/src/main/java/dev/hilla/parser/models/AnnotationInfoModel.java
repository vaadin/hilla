package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationInfo;

public abstract class AnnotationInfoModel implements Model, NamedModel {
    private ClassInfoModel classInfo;
    private Set<AnnotationParameterModel> parameters;

    public static AnnotationInfoModel of(@Nonnull AnnotationInfo annotation) {
        return new AnnotationInfoSourceModel(annotation);
    }

    public static AnnotationInfoModel of(@Nonnull Annotation annotation) {
        return new AnnotationInfoReflectionModel(annotation);
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
