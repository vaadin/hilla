package com.vaadin.fusion.parser.core;

import io.github.classgraph.AnnotationInfo;

public class RelativeAnnotationInfo
        extends AbstractRelative<AnnotationInfo, Relative<?>> {

    public RelativeAnnotationInfo(AnnotationInfo origin, Relative<?> parent) {
        super(origin, parent);
    }
}
