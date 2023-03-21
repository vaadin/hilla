package dev.hilla.parser.models;

import io.github.classgraph.PackageInfo;

public abstract class PackageInfoModel extends AnnotatedAbstractModel
        implements Model, NamedModel {
    public static PackageInfoModel of(Package origin) {
        return new PackageInfoReflectionModel(origin);
    }

    @Deprecated
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
    public Class<PackageInfoModel> getCommonModelClass() {
        return PackageInfoModel.class;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "PackageInfoModel[" + get() + "]";
    }
}
