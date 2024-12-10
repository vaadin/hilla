package com.vaadin.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationInfo;

public abstract class AnnotatedAbstractModel implements AnnotatedModel {
    private List<AnnotationInfoModel> annotations;

    protected static List<AnnotationInfoModel> processAnnotations(
            @Nonnull Annotation[] annotations) {
        return Arrays.stream(annotations).map(AnnotationInfoModel::of)
                .collect(Collectors.toList());
    }

    protected static List<AnnotationInfoModel> processAnnotations(
            List<AnnotationInfo> annotations) {
        return annotations != null ? processAnnotations(annotations.stream())
                : List.of();
    }

    protected static List<AnnotationInfoModel> processAnnotations(
            Stream<AnnotationInfo> annotations) {
        return annotations.map(AnnotationInfoModel::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = prepareAnnotations();
        }

        return annotations;
    }

    protected abstract List<AnnotationInfoModel> prepareAnnotations();
}
