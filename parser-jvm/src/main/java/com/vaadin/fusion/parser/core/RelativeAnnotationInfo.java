package com.vaadin.fusion.parser.core;

import javax.annotation.Nonnull;

import java.util.Objects;

import io.github.classgraph.AnnotationInfo;

public class RelativeAnnotationInfo
        extends AbstractRelative<AnnotationInfo, Relative<?>> {

    public RelativeAnnotationInfo(@Nonnull AnnotationInfo origin,
            @Nonnull Relative<?> parent) {
        super(origin, Objects.requireNonNull(parent));
    }
}
