package dev.hilla.parser.models;

import io.github.classgraph.ClassInfo;

final class ClassInfoSourceModel
        extends AbstractDependable<ClassInfo, Dependable<?, ?>>
        implements ClassInfoModel, SourceModel {
}
