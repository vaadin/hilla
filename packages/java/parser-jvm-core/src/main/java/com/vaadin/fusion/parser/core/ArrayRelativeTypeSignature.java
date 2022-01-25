package com.vaadin.fusion.parser.core;

import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.ClassInfo;

public final class ArrayRelativeTypeSignature
        extends AbstractRelative<ArrayTypeSignature, Relative<?>>
        implements RelativeTypeSignature {
    private RelativeTypeSignature nestedType;

    ArrayRelativeTypeSignature(ArrayTypeSignature origin, Relative<?> parent) {
        super(origin, parent);
    }

    public static Stream<ClassInfo> resolve(
            @Nonnull ArrayTypeSignature signature) {
        return RelativeTypeSignature.resolve(
                Objects.requireNonNull(signature).getElementTypeSignature());
    }

    public RelativeTypeSignature getNestedType() {
        if (nestedType == null) {
            nestedType = RelativeTypeSignature.of(origin.getNestedType(), this);
        }

        return nestedType;
    }

    @Override
    public boolean isArray() {
        return true;
    }
}
