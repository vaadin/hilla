package dev.hilla.parser.models;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

abstract class AbstractReflectionSignatureModel<T extends Type>
        extends AbstractModel<T> implements Dependable {
    private Collection<ClassInfoModel> dependencies;

    public AbstractReflectionSignatureModel(@Nonnull T origin, Model parent) {
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
