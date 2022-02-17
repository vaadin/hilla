package dev.hilla.parser.models;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import io.github.classgraph.TypeArgument;

final class TypeArgumentSourceModel extends AbstractModel<TypeArgument>
        implements TypeArgumentModel, SourceSignatureModel {
    private Collection<SignatureModel> wildcardAssociatedTypes;

    public TypeArgumentSourceModel(TypeArgument origin, Model parent) {
        super(origin, parent);
    }

    public Collection<SignatureModel> getAssociatedTypes() {
        if (wildcardAssociatedTypes == null) {
            var signature = origin.getTypeSignature();
            wildcardAssociatedTypes = signature == null ? Collections.emptySet()
                    : Set.of(SignatureModel.of(signature, this));
        }

        return wildcardAssociatedTypes;
    }

    public TypeArgument.Wildcard getWildcard() {
        return origin.getWildcard();
    }
}
