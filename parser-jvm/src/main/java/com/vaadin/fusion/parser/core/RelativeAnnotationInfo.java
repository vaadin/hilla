package com.vaadin.fusion.parser.core;

import io.github.classgraph.AnnotationInfo;

public class RelativeAnnotationInfo implements Relative, RelativeMember {
    private final AnnotationInfo origin;

    public RelativeAnnotationInfo(final AnnotationInfo origin) {
        this.origin = origin;
    }

    @Override
    public AnnotationInfo get() {
        return origin;
    }

    @Override
    public RelativeClassInfo getHost() {
        return new RelativeClassInfo(origin.getClassInfo());
    }
}
