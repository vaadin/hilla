package dev.hilla.parser.models;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.TypeParameter;

final class TypeParameterSourceModel
        extends AbstractSourceSignatureModel<TypeParameter>
        implements TypeParameterModel, SourceModel {
    private Collection<SignatureModel> bounds;

    public TypeParameterSourceModel(TypeParameter origin, Model parent) {
        super(origin, Objects.requireNonNull(parent));
    }

    @Override
    public Collection<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = Stream
                    .of(Stream.of(origin.getClassBound()),
                            origin.getInterfaceBounds().stream())
                    .flatMap(Function.identity())
                    .map(signature -> signature != null
                            ? SignatureModel.of(signature, this)
                            : null)
                    .collect(Collectors.toList());
        }

        return bounds;
    }
}
