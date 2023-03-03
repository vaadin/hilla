package dev.hilla.parser.models;

import java.util.List;

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

    /**
     * Returns a list of all ancestor packages, starting with the immediate
     * parent package. Note that not all packages are available in the
     * hierarchy, so the list can have "holes".
     *
     * @return the list of all valid ancestor packages
     */
    public abstract List<PackageInfoModel> getAncestors();

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "PackageInfoModel[" + get() + "]";
    }
}
