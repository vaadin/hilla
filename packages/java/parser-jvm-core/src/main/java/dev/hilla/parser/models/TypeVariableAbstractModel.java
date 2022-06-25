package dev.hilla.parser.models;

import java.util.List;

import javax.annotation.Nonnull;

abstract class TypeVariableAbstractModel<T> extends AbstractModel<T>
        implements TypeVariableModel {
    private List<AnnotationInfoModel> annotations;
    private TypeParameterModel typeParameter;

    TypeVariableAbstractModel(@Nonnull T origin) {
        super(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeVariableModel)) {
            return false;
        }

        var other = (TypeVariableModel) obj;

        return getName().equals(other.getName())
                && getAnnotations().equals(other.getAnnotations());
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = prepareAnnotations();
        }

        return annotations;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public SignatureModel resolve() {
        if (typeParameter == null) {
            typeParameter = prepareResolved();
        }

        return typeParameter;
    }

    protected abstract List<AnnotationInfoModel> prepareAnnotations();

    protected abstract TypeParameterModel prepareResolved();
}
