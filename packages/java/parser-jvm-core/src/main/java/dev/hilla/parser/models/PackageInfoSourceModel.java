package dev.hilla.parser.models;

import java.util.List;

import io.github.classgraph.PackageInfo;

class PackageInfoSourceModel extends PackageInfoModel {
    private final PackageInfo origin;

    PackageInfoSourceModel(PackageInfo origin) {
        this.origin = origin;
    }

    @Override
    public PackageInfo get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    protected List<AnnotationInfoModel> prepareAnnotations() {
        return processAnnotations(origin.getAnnotationInfo());
    }
}
