package dev.hilla.parser.models;

import java.util.Set;
import java.util.stream.Collectors;

import io.github.classgraph.AnnotationInfo;

final class AnnotationInfoSourceModel extends AbstractModel<AnnotationInfo>
        implements AnnotationInfoModel, SourceModel {
    private Set<AnnotationParameterModel> parameters;
    private ClassInfoModel resolved;

    public AnnotationInfoSourceModel(AnnotationInfo origin) {
        super(origin);
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
    public Set<AnnotationParameterModel> getParameters() {
        if (parameters == null) {
            parameters = origin.getParameterValues().stream()
                    .map(AnnotationParameterModel::of)
                    .collect(Collectors.toSet());
        }

        return parameters;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 11 * getParameters().hashCode();
    }
}
