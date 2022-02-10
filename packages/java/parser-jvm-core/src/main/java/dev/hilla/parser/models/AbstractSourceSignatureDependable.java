package dev.hilla.parser.models;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.github.classgraph.HierarchicalTypeSignature;

abstract class AbstractSourceSignatureDependable<T extends HierarchicalTypeSignature, P extends Dependable<?, ?>>
        extends AbstractDependable<T, P> {
    private Collection<ClassInfoModel> dependencies;

    public AbstractSourceSignatureDependable(@Nonnull T origin, P parent) {
        super(origin, parent);
    }

    @Override
    public Collection<ClassInfoModel> getDependencies() {
        if (dependencies == null) {
            dependencies = SourceSignatureModel.resolve(origin)
                    .map(ClassInfoModel::of).collect(Collectors.toList());
        }

        return dependencies;
    }
}
