package dev.hilla.parser.models;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.TypeParameter;

final class TypeParameterSourceModel
        extends AbstractSourceSignatureDependable<TypeParameter, Dependable<?, ?>>
        implements TypeParameterModel, SourceSignatureModel {
    private Collection<TypeModel> bounds;

    public TypeParameterSourceModel(TypeParameter origin, Dependable<?, ?> parent) {
        super(origin, Objects.requireNonNull(parent));
    }

    @Override
    public Collection<TypeModel> getBounds() {
        if (bounds == null) {
            bounds = Stream
                    .of(Stream.of(origin.getClassBound()),
                            origin.getInterfaceBounds().stream())
                    .flatMap(Function.identity())
                    .map(signature -> signature != null
                            ? SourceSignatureModel.of(signature, this)
                            : null)
                    .collect(Collectors.toList());
        }

        return bounds;
    }
}
