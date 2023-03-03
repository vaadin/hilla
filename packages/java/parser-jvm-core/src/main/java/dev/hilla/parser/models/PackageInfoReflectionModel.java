package dev.hilla.parser.models;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PackageInfoReflectionModel extends PackageInfoModel {
    private final Package origin;

    PackageInfoReflectionModel(Package origin) {
        this.origin = origin;
    }

    @Override
    public Package get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getDeclaredAnnotations());
    }

    @Override
    public List<PackageInfoModel> getAncestors() {
        var classLoader = getClass().getClassLoader();
        return getAllAncestorPackageNames(origin.getName())
                .map(classLoader::getDefinedPackage).filter(Objects::nonNull)
                .map(PackageInfoModel::of).collect(Collectors.toList());
    }

    /**
     * Returns a stream of all ancestor package names, starting with the
     * immediate parent package.
     *
     * @param packageName
     *            the package name
     * @return the stream of all ancestor package names
     */
    private static Stream<String> getAllAncestorPackageNames(
            String packageName) {
        return Stream.iterate(packageName, p -> p.contains("."),
                p -> p.substring(0, p.lastIndexOf('.')));
    }
}
