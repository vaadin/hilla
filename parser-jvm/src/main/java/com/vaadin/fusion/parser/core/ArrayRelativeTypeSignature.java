package com.vaadin.fusion.parser.core;

import java.util.stream.Stream;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.ClassInfo;

public final class ArrayRelativeTypeSignature
        extends AbstractRelative<ArrayTypeSignature, Relative<?>>
        implements RelativeTypeSignature {
    ArrayRelativeTypeSignature(ArrayTypeSignature origin,
            Relative<?> parent) {
        super(origin, parent);
    }

    public static Stream<ClassInfo> resolve(ArrayTypeSignature signature) {
        return RelativeTypeSignature
                .resolve(signature.getElementTypeSignature());
    }

    @Override
    public boolean isArray() {
        return true;
    }

    public RelativeTypeSignature getNestedType() {
        return RelativeTypeSignature.of(origin.getNestedType(), this);
    }
}
