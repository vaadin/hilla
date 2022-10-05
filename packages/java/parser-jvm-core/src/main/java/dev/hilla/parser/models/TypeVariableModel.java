package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.util.List;
import java.util.Objects;

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
     * @return Type variable model instance.
     * @deprecated To be removed once <a href=
     *             "https://github.com/classgraph/classgraph/issues/706">{@code
     * TypeVariable.resolve()} </a> is fixed.
     */
    @Deprecated
    public static TypeVariableModel of(
            @Nonnull TypeParameterModel typeParameter,
            @Nonnull List<AnnotationInfoModel> annotations) {
        return new TypeVariableArtificialModel(typeParameter, annotations);
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
                && getAnnotations().equals(other.getAnnotations())
                && resolve().equals(other.resolve());
    }

    @Override
    public Class<TypeVariableModel> getCommonModelClass() {
        return TypeVariableModel.class;
    }

    @Override
    public int hashCode() {
        return 0x4f76c9f1 ^ getName().hashCode();
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

    @Override
    public String toString() {
        return getName();
    }

    protected abstract TypeParameterModel prepareResolved();
}
