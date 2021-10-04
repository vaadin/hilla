package com.vaadin.fusion.parser;

import io.github.classgraph.AnnotationInfo;

public class RelativeAnnotationInfo implements Relative {
    private final AnnotationInfo annotationInfo;

    RelativeAnnotationInfo(final AnnotationInfo annotationInfo) {
        this.annotationInfo = annotationInfo;
    }

    @Override
    public AnnotationInfo get() {
        return annotationInfo;
    }
}
