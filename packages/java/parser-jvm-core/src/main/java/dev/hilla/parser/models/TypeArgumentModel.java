package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeSignature;

public abstract class TypeArgumentModel extends AnnotatedAbstractModel
        implements SignatureModel {
    private List<SignatureModel> associatedTypes;

    public static TypeArgumentModel of(@Nonnull TypeArgument origin) {
        return new TypeArgumentSourceModel(Objects.requireNonNull(origin));
    }

    public static TypeArgumentModel of(@Nonnull AnnotatedType origin) {
        return new TypeArgumentReflectionModel(Objects.requireNonNull(origin));
    }

    /**
     * A factory method that creates an artificial type argument from the
     * provided arguments.
     *
     * @param wildcard
     *            Wildcard kind for the type argument.
     * @param associatedTypes
     *            List of types to associate with the type argument.
     * @param annotations
     *            List of type argument annotations.
     * @return The type argument.
     * @deprecated To be removed once <a href=
     *             "https://github.com/classgraph/classgraph/issues/706">{@code
     * TypeVariable.resolve()} </a> is fixed.
     */
    @Deprecated
    public static TypeArgumentModel of(@Nonnull TypeArgument.Wildcard wildcard,
            @Nonnull List<SignatureModel> associatedTypes,
            @Nonnull List<AnnotationInfoModel> annotations) {
        return new TypeArgumentArtificialModel(wildcard, associatedTypes,
                annotations);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeArgumentModel)) {
            return false;
        }

        var other = (TypeArgumentModel) obj;

        return getAnnotations().equals(other.getAnnotations())
                && getAssociatedTypes().equals(other.getAssociatedTypes())
                && getWildcard().equals(other.getWildcard());
    }

    public List<SignatureModel> getAssociatedTypes() {
        if (associatedTypes == null) {
            associatedTypes = prepareAssociatedTypes();
        }

        return associatedTypes;
    }

    public Stream<SignatureModel> getAssociatedTypesStream() {
        return getAssociatedTypes().stream();
    }

    @Override
    public Class<TypeArgumentModel> getCommonModelClass() {
        return TypeArgumentModel.class;
    }

    public abstract TypeArgument.Wildcard getWildcard();

    @Override
    public int hashCode() {
        return getAssociatedTypes().hashCode() + 7 * getWildcard().hashCode();
    }

    @Override
    public boolean isTypeArgument() {
        return true;
    }

    @Override
    public String toString() {
        return getAssociatedTypesStream().map(Object::toString)
                .collect(Collectors.joining(" & "));
    }

    protected abstract List<SignatureModel> prepareAssociatedTypes();
}
