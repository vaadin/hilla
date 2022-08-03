package dev.hilla.parser.models;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.utils.Streams;

import io.github.classgraph.TypeParameter;

final class TypeParameterSourceModel extends TypeParameterModel
        implements SourceSignatureModel {
    private final TypeParameter origin;

    TypeParameterSourceModel(TypeParameter origin) {
        this.origin = origin;
    }

    @Override
    public TypeParameter get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return List.of();
    }

    @Override
    protected List<SignatureModel> prepareBounds() {
        return Streams
                .combine(Stream.of(origin.getClassBound()),
                        origin.getInterfaceBounds().stream())
                .filter(Objects::nonNull).map(SignatureModel::of).distinct()
                .collect(Collectors.toList());
    }
}
