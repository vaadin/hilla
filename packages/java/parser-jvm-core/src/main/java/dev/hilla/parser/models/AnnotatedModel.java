package dev.hilla.parser.models;

import java.util.List;
import java.util.stream.Stream;

public interface AnnotatedModel {
    List<AnnotationInfoModel> getAnnotations();

    default Stream<AnnotationInfoModel> getAnnotationsStream() {
        return getAnnotations().stream();
    }
}
