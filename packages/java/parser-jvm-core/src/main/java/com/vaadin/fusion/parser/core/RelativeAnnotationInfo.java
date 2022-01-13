package com.vaadin.fusion.parser.core;

import java.util.Objects;

import javax.annotation.Nonnull;

import io.github.classgraph.AnnotationInfo;

public final class RelativeAnnotationInfo
        extends AbstractRelative<AnnotationInfo, Relative<?>> {

    private RelativeAnnotationInfo(@Nonnull AnnotationInfo origin,
            @Nonnull Relative<?> parent) {
        super(origin, Objects.requireNonNull(parent));
    }

    public static RelativeAnnotationInfo of(@Nonnull AnnotationInfo origin,
            Relative<?> parent) {
        return Pool.createInstance(origin, parent, RelativeAnnotationInfo::new);
    }
}
