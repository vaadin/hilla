package dev.hilla.parser.models;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.github.classgraph.HierarchicalTypeSignature;

abstract class AbstractSourceSignatureModel<T extends HierarchicalTypeSignature>
        extends AbstractModel<T> implements Dependable {
    private Collection<ClassInfoModel> dependencies;

    public AbstractSourceSignatureModel(@Nonnull T origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public Collection<ClassInfoModel> getDependencies() {
        if (dependencies == null) {
            dependencies = SignatureModel.resolveDependencies(origin)
                    .map(ClassInfoModel::of).collect(Collectors.toSet());
        }

        return dependencies;
    }
}
