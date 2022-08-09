package dev.hilla.parser.models;

import java.util.List;
import java.util.stream.Collectors;

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
    public TypeArgument.Wildcard getWildcard() {
        return origin.getWildcard();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return getAssociatedTypes().stream()
                .flatMap(SignatureModel::getAnnotationsStream)
                .collect(Collectors.toList());
    }

    @Override
    protected List<SignatureModel> prepareAssociatedTypes() {
        var signature = origin.getTypeSignature();

        return signature == null ? List.of()
                : List.of(SignatureModel.of(signature));
    }
}
