package dev.hilla.parser.models;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.github.classgraph.TypeArgument;

final class TypeArgumentSourceModel extends AbstractModel<TypeArgument>
        implements TypeArgumentModel, SourceSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private List<SignatureModel> associatedTypes;

    public TypeArgumentSourceModel(TypeArgument origin) {
        super(origin);
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

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = getAssociatedTypes().stream()
                    .flatMap(model -> model.getAnnotations().stream())
                    .collect(Collectors.toList());
        }

        return annotations;
    }

    public List<SignatureModel> getAssociatedTypes() {
        if (associatedTypes == null) {
            var signature = origin.getTypeSignature();
            associatedTypes = signature == null ? Collections.emptyList()
                    : List.of(SignatureModel.of(signature));
        }

        return associatedTypes;
    }

    public TypeArgument.Wildcard getWildcard() {
        return origin.getWildcard();
    }

    @Override
    public int hashCode() {
        return getAssociatedTypes().hashCode() + 7 * getWildcard().hashCode();
    }
}
