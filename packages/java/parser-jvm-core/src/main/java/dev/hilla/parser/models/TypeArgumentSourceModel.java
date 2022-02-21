package dev.hilla.parser.models;

import java.util.Collections;
import java.util.List;

import io.github.classgraph.TypeArgument;

final class TypeArgumentSourceModel extends AbstractModel<TypeArgument>
        implements TypeArgumentModel, SourceSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private List<SignatureModel> wildcardAssociatedTypes;

    public TypeArgumentSourceModel(TypeArgument origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = AnnotationUtils.processTypeAnnotations(
                    origin.getTypeSignature().getTypeAnnotationInfo(), parent);
        }

        return annotations;
    }

    public List<SignatureModel> getAssociatedTypes() {
        if (wildcardAssociatedTypes == null) {
            var signature = origin.getTypeSignature();
            wildcardAssociatedTypes = signature == null
                    ? Collections.emptyList()
                    : List.of(SignatureModel.of(signature, this));
        }

        return wildcardAssociatedTypes;
    }

    public TypeArgument.Wildcard getWildcard() {
        return origin.getWildcard();
    }
}
