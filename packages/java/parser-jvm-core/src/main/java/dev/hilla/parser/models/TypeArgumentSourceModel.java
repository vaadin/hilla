package dev.hilla.parser.models;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.TypeArgument;

final class TypeArgumentSourceModel extends
        AbstractSourceSignatureDependable<TypeArgument, Dependable<?, ?>>
        implements TypeArgumentModel, SourceSignatureModel {
    private Set<TypeModel> wildcardAssociatedTypes;

    public TypeArgumentSourceModel(TypeArgument origin,
            Dependable<?, ?> parent) {
        super(origin, parent);
    }

    public TypeArgument.Wildcard getWildcard() {
        return origin.getWildcard();
    }

    public Collection<TypeModel> getWildcardAssociatedTypes() {
        if (wildcardAssociatedTypes == null) {
            wildcardAssociatedTypes = Set.of(SourceSignatureModel
                    .ofNullable(origin.getTypeSignature(), this));
        }

        return wildcardAssociatedTypes;
    }
}
