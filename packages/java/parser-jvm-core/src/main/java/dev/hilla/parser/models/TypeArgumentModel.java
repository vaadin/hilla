package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.TypeArgument;

public abstract class TypeArgumentModel extends AnnotatedAbstractModel
        implements SignatureModel {
    private List<SignatureModel> associatedTypes;

    @Deprecated
    public static TypeArgumentModel of(@Nonnull TypeArgument origin) {
        return new TypeArgumentSourceModel(Objects.requireNonNull(origin));
    }

    public static TypeArgumentModel of(@Nonnull AnnotatedType origin) {
        return new TypeArgumentReflectionModel(Objects.requireNonNull(origin));
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

    public abstract Wildcard getWildcard();

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

    public enum Wildcard {
        NONE, ANY, EXTENDS, SUPER;
    }
}
