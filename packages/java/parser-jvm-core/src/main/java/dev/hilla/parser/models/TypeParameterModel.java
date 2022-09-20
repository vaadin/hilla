package dev.hilla.parser.models;

import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.TypeParameter;

public abstract class TypeParameterModel extends AnnotatedAbstractModel
        implements SignatureModel, NamedModel {
    private List<SignatureModel> bounds;

    public static TypeParameterModel of(@Nonnull TypeParameter origin) {
        return new TypeParameterSourceModel(Objects.requireNonNull(origin));
    }

    public static TypeParameterModel of(@Nonnull TypeVariable<?> origin) {
        return new TypeParameterReflectionModel(Objects.requireNonNull(origin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TypeParameterModel)) {
            return false;
        }

        var other = (TypeParameterModel) obj;

        return getName().equals(other.getName())
                && getAnnotations().equals(other.getAnnotations())
                && getBounds().equals(other.getBounds());
    }

    public List<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = prepareBounds();
        }

        return bounds;
    }

    public Stream<SignatureModel> getBoundsStream() {
        return getBounds().stream();
    }

    @Override
    public Class<TypeParameterModel> getCommonModelClass() {
        return TypeParameterModel.class;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 3 * getBounds().hashCode();
    }

    @Override
    public boolean isTypeParameter() {
        return true;
    }

    protected abstract List<SignatureModel> prepareBounds();
}
