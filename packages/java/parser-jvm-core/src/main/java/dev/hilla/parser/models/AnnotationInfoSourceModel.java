package dev.hilla.parser.models;

import java.util.Set;
import java.util.stream.Collectors;

import io.github.classgraph.AnnotationInfo;

final class AnnotationInfoSourceModel extends AnnotationInfoModel
        implements SourceModel {
    private final AnnotationInfo origin;

    AnnotationInfoSourceModel(AnnotationInfo origin) {
        this.origin = origin;
    }

    @Override
    public AnnotationInfo get() {
        return origin;
    }

    @Override
    public String getName() {
        return origin.getName();
    }

    @Override
    protected ClassInfoModel prepareClassInfo() {
        return ClassInfoModel.of(origin.getClassInfo());
    }

    @Override
    protected Set<AnnotationParameterModel> prepareParameters() {
        return origin.getParameterValues().stream()
                .map(AnnotationParameterModel::of).collect(Collectors.toSet());
    }
}
