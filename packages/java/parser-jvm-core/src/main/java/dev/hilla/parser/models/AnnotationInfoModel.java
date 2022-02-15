package dev.hilla.parser.models;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;

import io.github.classgraph.AnnotationInfo;

public interface AnnotationInfoModel extends Model, Dependable {
    static AnnotationInfoModel of(@Nonnull AnnotationInfo annotation, @Nonnull Model parent) {
        return new AnnotationInfoSourceModel(annotation, parent);
    }

    @Override
    default Collection<ClassInfoModel> getDependencies() {
        return Collections.emptySet();
    }
}
