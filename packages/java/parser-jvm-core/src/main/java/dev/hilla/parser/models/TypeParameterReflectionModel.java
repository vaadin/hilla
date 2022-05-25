package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class TypeParameterReflectionModel
        extends AbstractAnnotatedReflectionModel<AnnotatedTypeVariable>
        implements TypeParameterModel, ReflectionSignatureModel {
    private List<SignatureModel> bounds;

    public TypeParameterReflectionModel(AnnotatedTypeVariable origin) {
        super(origin);
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

    @Override
    public List<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = Arrays.stream(origin.getAnnotatedBounds())
                    .map(SignatureModel::of).collect(Collectors.toList());
        }

        return bounds;
    }

    @Override
    public String getName() {
        return ((TypeVariable<?>) origin.getType()).getName();
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + 3 * getBounds().hashCode();
    }
}
