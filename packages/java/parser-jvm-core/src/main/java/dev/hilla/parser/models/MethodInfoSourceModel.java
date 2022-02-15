package dev.hilla.parser.models;

import io.github.classgraph.MethodInfo;

final class MethodInfoSourceModel extends AbstractModel<MethodInfo>
        implements MethodInfoModel, SourceModel {
    public MethodInfoSourceModel(MethodInfo method, Model parent) {
        super(method, parent);
    }
}
