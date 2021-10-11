package com.vaadin.fusion.parser.core;

import io.github.classgraph.AnnotationInfo;

public class RelativeAnnotationInfo implements Relative, RelativeMember {
    private final AnnotationInfo annotationInfo;
    private final RelativeClassInfo host;

    RelativeAnnotationInfo(final AnnotationInfo annotationInfo) {
        this.annotationInfo = annotationInfo;
        host = new RelativeClassInfo(annotationInfo.getClassInfo());
    }

    @Override
    public AnnotationInfo get() {
        return annotationInfo;
    }

    @Override
    public RelativeClassInfo getHost() {
        return host;
    }
}
