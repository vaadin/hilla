package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationInfo;

public interface AnnotationInfoModel extends Model, NamedModel {
    static AnnotationInfoModel of(@Nonnull AnnotationInfo annotation,
            @Nonnull Model parent) {
        return new AnnotationInfoSourceModel(annotation, parent);
    }

    static AnnotationInfoModel of(@Nonnull Annotation annotation,
            @Nonnull Model parent) {
        return new AnnotationInfoReflectionModel(annotation, parent);
    }

    @Override
    default Stream<ClassInfoModel> getDependenciesStream() {
        return Stream.empty();
    }
}
