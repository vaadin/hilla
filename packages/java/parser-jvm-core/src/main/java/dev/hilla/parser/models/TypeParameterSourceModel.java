package dev.hilla.parser.models;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.utils.StreamUtils;

import io.github.classgraph.TypeParameter;

final class TypeParameterSourceModel extends AbstractModel<TypeParameter>
        implements TypeParameterModel, SourceSignatureModel {
    private Collection<SignatureModel> bounds;

    public TypeParameterSourceModel(TypeParameter origin, Model parent) {
        super(origin, Objects.requireNonNull(parent));
    }

    @Override
    public Collection<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = StreamUtils
                    .combine(Stream.of(origin.getClassBound()),
                            origin.getInterfaceBounds().stream())
                    .map(signature -> signature != null
                            ? SignatureModel.of(signature, this)
                            : null)
                    .collect(Collectors.toList());
        }

        return bounds;
    }
}
