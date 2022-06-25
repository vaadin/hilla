package dev.hilla.parser.models;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationInfo;

final class AnnotationUtils {
    static List<AnnotationInfoModel> convert(
            @Nonnull Annotation[] annotations) {
        return Arrays.stream(annotations).map(AnnotationInfoModel::of)
                .collect(Collectors.toList());
    }

    static List<AnnotationInfoModel> convert(List<AnnotationInfo> annotations) {
        return annotations != null ? convert(annotations.stream()) : List.of();
    }

    static List<AnnotationInfoModel> convert(
            Stream<AnnotationInfo> annotations) {
        return annotations.map(AnnotationInfoModel::of)
                .collect(Collectors.toList());
    }
}
