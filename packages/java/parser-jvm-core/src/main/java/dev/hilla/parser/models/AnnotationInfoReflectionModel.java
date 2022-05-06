package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.util.Objects;

final class AnnotationInfoReflectionModel extends AbstractModel<Annotation>
        implements AnnotationInfoModel, ReflectionModel {
    public AnnotationInfoReflectionModel(Annotation annotation) {
        super(annotation);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AnnotationInfoModel)) {
            return false;
        }

        if (other instanceof AnnotationInfoReflectionModel) {
            return Objects.equals(origin,
                    ((AnnotationInfoReflectionModel) other).origin);
        }

        return Objects.equals(getName(),
                ((AnnotationInfoModel) other).getName());
    }

    @Override
    public String getName() {
        return origin.annotationType().getName();
    }
}
