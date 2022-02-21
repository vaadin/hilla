package dev.hilla.parser.models;

import java.lang.annotation.Annotation;

final class AnnotationInfoReflectionModel extends AbstractModel<Annotation>
        implements AnnotationInfoModel, ReflectionModel {
    public AnnotationInfoReflectionModel(Annotation annotation, Model parent) {
        super(annotation, parent);
    }

    @Override
    public String getName() {
        return origin.annotationType().getName();
    }
}
