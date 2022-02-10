package dev.hilla.parser.models;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

abstract class AbstractReflectionSignatureDependable<T extends Type, P extends Dependable<?, ?>>
    extends AbstractDependable<T, P> {
    private Collection<ClassInfoModel> dependencies;

    public AbstractReflectionSignatureDependable(@Nonnull T origin, P parent) {
        super(origin, parent);
    }

    @Override
    public Collection<ClassInfoModel> getDependencies() {
        if (dependencies == null) {
            dependencies = ReflectionSignatureModel.resolve(origin)
                .map(ClassInfoModel::of).collect(Collectors.toList());
        }

        return dependencies;
    }
}
