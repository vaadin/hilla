package dev.hilla.parser.models;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.utils.Streams;

import io.github.classgraph.TypeParameter;

final class TypeParameterSourceModel extends AbstractModel<TypeParameter>
        implements TypeParameterModel, SourceSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private List<SignatureModel> bounds;

    public TypeParameterSourceModel(TypeParameter origin) {
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

        return origin.getName().equals(other.getName())
                && getAnnotations().equals(other.getAnnotations())
                && getBounds().equals(other.getBounds());
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = List.of();
        }

        return annotations;
    }

    @Override
    public List<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = Streams
                    .combine(Stream.of(origin.getClassBound()),
                            origin.getInterfaceBounds().stream())
                    .map(signature -> signature != null
                            ? SignatureModel.of(signature)
                            : null)
                    .distinct().collect(Collectors.toList());
        }

        return bounds;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    public int hashCode() {
        return origin.getName().hashCode() + 3 * getBounds().hashCode();
    }
}
