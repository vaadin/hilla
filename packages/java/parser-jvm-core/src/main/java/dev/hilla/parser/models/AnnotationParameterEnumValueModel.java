package dev.hilla.parser.models;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationEnumValue;

public interface AnnotationParameterEnumValueModel extends Model {
    static AnnotationParameterEnumValueModel of(
            @Nonnull AnnotationEnumValue origin) {
        return new AnnotationParameterEnumValueSourceModel(origin);
    }

    static AnnotationParameterEnumValueModel of(@Nonnull Enum<?> origin) {
        return new AnnotationParameterEnumValueReflectionModel(origin);
    }

    ClassInfoModel getClassInfo();

    String getValueName();
}
