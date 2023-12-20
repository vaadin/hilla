package com.vaadin.hilla.parser.models;

import java.util.Optional;
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
    protected Optional<ClassInfoModel> prepareClassInfo() {
        var cls = origin.getClassInfo();

        return cls != null ? Optional.of(ClassInfoModel.of(cls))
                : Optional.empty();
    }

    @Override
    protected Set<AnnotationParameterModel> prepareParameters() {
        return origin.getParameterValues().stream()
                .map(AnnotationParameterModel::of).collect(Collectors.toSet());
    }
}
