package dev.hilla.parser.models;

import java.util.List;
import java.util.stream.Stream;

public interface AnnotatedModel {
    List<AnnotationInfoModel> getAnnotations();

    void setAnnotations(List<AnnotationInfoModel> annotations);

    default Stream<AnnotationInfoModel> getAnnotationsStream() {
        return getAnnotations().stream();
    }
}
