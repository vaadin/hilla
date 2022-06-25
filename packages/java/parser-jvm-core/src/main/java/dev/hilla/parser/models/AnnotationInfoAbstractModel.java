package dev.hilla.parser.models;

import java.util.Set;

import javax.annotation.Nonnull;

abstract class AnnotationInfoAbstractModel<T> extends AbstractModel<T>
        implements AnnotationInfoModel {
    private Set<AnnotationParameterModel> parameters;
    private ClassInfoModel resolved;

    AnnotationInfoAbstractModel(@Nonnull T origin) {
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
            resolved = prepareClassInfo();
        }

        return resolved;
    }

    @Override
    public Set<AnnotationParameterModel> getParameters() {
        if (parameters == null) {
            parameters = prepareParameters();
        }

        return parameters;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 11 * getParameters().hashCode();
    }

    protected abstract ClassInfoModel prepareClassInfo();

    protected abstract Set<AnnotationParameterModel> prepareParameters();
}
