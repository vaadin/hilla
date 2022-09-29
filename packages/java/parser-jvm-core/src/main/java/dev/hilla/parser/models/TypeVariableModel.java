package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.List;
import java.util.Objects;

import javax.annotation.Detainted;
import javax.annotation.Nonnull;

import io.github.classgraph.TypeVariableSignature;

public abstract class TypeVariableModel extends AnnotatedAbstractModel
        implements SignatureModel, NamedModel {
    private TypeParameterModel typeParameter;

    public static TypeVariableModel of(@Nonnull TypeVariableSignature origin) {
        return new TypeVariableSourceModel(Objects.requireNonNull(origin));
    }

    public static TypeVariableModel of(@Nonnull AnnotatedTypeVariable origin) {
        return new TypeVariableReflectionModel(Objects.requireNonNull(origin));
    }

    /**
     * A factory that creates a synthetic pre-resolved type variable model.
     *
     * @param typeParameter
     *            Origin type parameter.
     * @param annotations
     *            List of variable annotations.
     * @return Type variable model instance
     * @deprecated To be removed once <a href=
     *             "https://github.com/classgraph/classgraph/issues/706">{@code
     * TypeVariable.resolve()} </a> is fixed.
     */
    @Deprecated
    public static TypeVariableModel of(
            @Nonnull TypeParameterModel typeParameter,
            @Nonnull List<AnnotationInfoModel> annotations) {
        return new TypeVariableSyntheticModel(typeParameter, annotations);
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
    public Class<TypeVariableModel> getCommonModelClass() {
        return TypeVariableModel.class;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommonModelClass().getName(), getName(),
                getAnnotations());
    }

    @Override
    public boolean isTypeVariable() {
        return true;
    }

    public TypeParameterModel resolve() {
        if (typeParameter == null) {
            typeParameter = prepareResolved();
        }

        return typeParameter;
    }

    protected abstract TypeParameterModel prepareResolved();
}
