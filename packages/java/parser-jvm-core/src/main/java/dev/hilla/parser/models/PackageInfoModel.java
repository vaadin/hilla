package dev.hilla.parser.models;

import java.util.Set;
import java.util.stream.Stream;

import io.github.classgraph.PackageInfo;

public abstract class PackageInfoModel extends AnnotatedAbstractModel
        implements Model, NamedModel {
    public static PackageInfoModel of(Package origin) {
        return new PackageInfoReflectionModel(origin);
    }

    public static PackageInfoModel of(PackageInfo origin) {
        return new PackageInfoSourceModel(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PackageInfoModel)) {
            return false;
        }

        var other = (PackageInfoModel) obj;

        return getName().equals(other.getName());
    }

    @Override
    public Set<ClassInfoModel> getDependencies() {
        return Set.of();
    }

    @Override
    public Stream<ClassInfoModel> getDependenciesStream() {
        return Stream.empty();
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
