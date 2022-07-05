package dev.hilla.parser.models;

import java.util.Map;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationParameterValue;

public interface AnnotationParameterModel extends Model, NamedModel {
    static AnnotationParameterModel of(@Nonnull String name,
            @Nonnull Object value) {
        return of(Map.entry(name, value));
    }

    static <T> AnnotationParameterModel of(
            @Nonnull Map.Entry<String, T> origin) {
        return new AnnotationParameterReflectionModel<>(origin);
    }

    static AnnotationParameterModel of(
            @Nonnull AnnotationParameterValue origin) {
        return new AnnotationParameterSourceModel(origin);
    }

    Object getValue();
}
