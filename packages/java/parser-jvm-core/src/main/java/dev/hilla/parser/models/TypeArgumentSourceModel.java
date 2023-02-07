package dev.hilla.parser.models;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.TypeArgument;

final class TypeArgumentSourceModel extends TypeArgumentModel
        implements SourceSignatureModel {
    private final TypeArgument origin;

    TypeArgumentSourceModel(TypeArgument origin) {
        this.origin = origin;
    }

    @Override
    public TypeArgument get() {
        return origin;
    }

    @Override
    public Wildcard getWildcard() {
        switch (origin.getWildcard()) {
        case EXTENDS:
            return Wildcard.EXTENDS;
        case ANY:
            return Wildcard.ANY;
        case SUPER:
            return Wildcard.SUPER;
        default:
            return Wildcard.NONE;
        }
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        var annotations = origin.getTypeAnnotationInfo();

        return Stream.concat(
                getAssociatedTypes().stream()
                        .map(SignatureModel::getAnnotations)
                        .flatMap(Collection::stream),
                annotations != null
                        ? annotations.stream().map(AnnotationInfoModel::of)
                        : Stream.empty())
                .collect(Collectors.toList());
    }

    @Override
    protected List<SignatureModel> prepareAssociatedTypes() {
        var signature = origin.getTypeSignature();

        return signature == null ? List.of()
                : List.of(SignatureModel.of(signature));
    }
}
