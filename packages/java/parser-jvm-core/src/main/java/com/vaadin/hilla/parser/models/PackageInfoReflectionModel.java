package com.vaadin.hilla.parser.models;

import java.util.List;

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
}
