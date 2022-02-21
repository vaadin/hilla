package dev.hilla.parser.models;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.classgraph.AnnotationInfoList;

final class AnnotationUtils {
    public static List<AnnotationInfoModel> processTypeAnnotations(
            AnnotationInfoList annotations, Model parent) {
        return annotations != null
                ? annotations.stream()
                        .map(annotation -> AnnotationInfoModel.of(annotation,
                                parent))
                        .collect(Collectors.toList())
                : List.of();
    }

    public static List<AnnotationInfoModel> processTypeAnnotations(
            AnnotatedElement origin, Model parent) {
        return Arrays.stream(origin.getAnnotations())
                .map(annotation -> AnnotationInfoModel.of(annotation, parent))
                .collect(Collectors.toList());
    }
}
