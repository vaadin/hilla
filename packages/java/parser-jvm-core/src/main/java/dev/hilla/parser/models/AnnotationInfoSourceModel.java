package dev.hilla.parser.models;

import java.util.Objects;

import io.github.classgraph.AnnotationInfo;

final class AnnotationInfoSourceModel extends AbstractModel<AnnotationInfo>
        implements AnnotationInfoModel, SourceModel {
    public AnnotationInfoSourceModel(AnnotationInfo origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AnnotationInfoModel)) {
            return false;
        }

        if (other instanceof AnnotationInfoSourceModel) {
            return Objects.equals(origin,
                    ((AnnotationInfoSourceModel) other).origin);
        }

        return Objects.equals(getName(),
                ((AnnotationInfoModel) other).getName());
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public int hashCode() {
        return origin.getName().hashCode();
    }
}
