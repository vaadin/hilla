package dev.hilla.parser.models;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.classgraph.AnnotationInfo;

final class AnnotationInfoSourceModel extends AbstractModel<AnnotationInfo>
        implements AnnotationInfoModel, SourceModel {
    private Set<AnnotationParameter> parameters;
    private ClassInfoModel resolved;

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

        return Objects.equals(getName(),
                ((AnnotationInfoModel) other).getName());
    }

    @Override
    public ClassInfoModel getClassInfo() {
        if (resolved == null) {
            resolved = ClassInfoModel.of(origin.getClassInfo());
        }

        return resolved;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public Set<AnnotationParameter> getParameters() {
        if (parameters == null) {
            parameters = origin.getParameterValues().stream()
                    .map(AnnotationParameter::new).collect(Collectors.toSet());
        }

        return parameters;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 11 * getParameters().hashCode();
    }
}
