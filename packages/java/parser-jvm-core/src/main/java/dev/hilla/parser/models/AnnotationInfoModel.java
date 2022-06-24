package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationInfo;

public interface AnnotationInfoModel extends Model, NamedModel {
    static AnnotationInfoModel of(@Nonnull AnnotationInfo annotation) {
        return new AnnotationInfoSourceModel(annotation);
    }

    static AnnotationInfoModel of(@Nonnull Annotation annotation) {
        return new AnnotationInfoReflectionModel(annotation);
    }

    ClassInfoModel getClassInfo();

    @Override
    default Stream<ClassInfoModel> getDependenciesStream() {
        return Stream.empty();
    }

    Set<AnnotationParameterModel> getParameters();
}
