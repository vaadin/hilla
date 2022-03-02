package dev.hilla.parser.models;

import io.github.classgraph.AnnotationInfo;

final class AnnotationInfoSourceModel extends AbstractModel<AnnotationInfo>
        implements AnnotationInfoModel, SourceModel {
    public AnnotationInfoSourceModel(AnnotationInfo origin, Model parent) {
        super(origin, parent);
    }

    @Override
    public String getName() {
        return origin.getName();
    }
}
